import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { usersApi } from '../../api/usersApi'
import { BulkImportDialog } from './BulkImportDialog'

vi.mock('../../api/usersApi', () => ({
  usersApi: {
    importUsers: vi.fn(),
  },
}))

function getDialog() {
  return within(screen.getByRole('dialog'))
}

function getFileInput() {
  return document.getElementById('bulk-import-file-input') as HTMLInputElement
}

function createCsvFile(content: string, name = 'users.csv') {
  return new File([content], name, { type: 'text/csv' })
}

describe('BulkImportDialog', () => {
  const onClose = vi.fn()
  const onComplete = vi.fn()
  const onDownloadTemplate = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('documents valid role values in the import dialog', () => {
    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    expect(screen.getByText('Valid role values: ADMIN, EMPLOYEE')).toBeInTheDocument()
  })

  it('excludes comma-only trailing rows from preview row count', async () => {
    const user = userEvent.setup()
    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    await user.upload(
      getFileInput(),
      createCsvFile(
        'Employee ID,Full Name,Email,Role\nEMP010,Jane Doe,jane@example.com,EMPLOYEE\n,,,\n , , ,\n',
      ),
    )

    expect(await getDialog().findByText('1 data row detected.')).toBeInTheDocument()
  })

  it('shows preview with filename and row count before import', async () => {
    const user = userEvent.setup()
    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    const file = createCsvFile(
      'Employee ID,Full Name,Email,Role\nEMP010,Jane Doe,jane@example.com,EMPLOYEE\n',
    )
    await user.upload(getFileInput(), file)

    expect(await getDialog().findByText('users.csv')).toBeInTheDocument()
    expect(getDialog().getByText('1 data row detected.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Import users' })).toBeEnabled()
    expect(usersApi.importUsers).not.toHaveBeenCalled()
  })

  it('imports after confirmation and shows results', async () => {
    const user = userEvent.setup()
    vi.mocked(usersApi.importUsers).mockResolvedValue({
      totalRows: 1,
      imported: 1,
      failed: 0,
      errors: [],
    })

    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    await user.upload(
      getFileInput(),
      createCsvFile('Employee ID,Full Name,Email,Role\nEMP010,Jane Doe,jane@example.com,EMPLOYEE\n'),
    )
    await user.click(await screen.findByRole('button', { name: 'Import users' }))

    expect(await getDialog().findByText('Imported: 1')).toBeInTheDocument()
    expect(usersApi.importUsers).toHaveBeenCalledTimes(1)
    expect(onComplete).toHaveBeenCalledWith({
      totalRows: 1,
      imported: 1,
      failed: 0,
      errors: [],
    })
  })

  it('disables import, close, and file picker while uploading', async () => {
    const user = userEvent.setup()
    let resolveImport: (value: {
      totalRows: number
      imported: number
      failed: number
      errors: string[]
    }) => void = () => undefined
    vi.mocked(usersApi.importUsers).mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveImport = resolve
        }),
    )

    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    await user.upload(
      getFileInput(),
      createCsvFile('Employee ID,Full Name,Email,Role\nEMP010,Jane Doe,jane@example.com,EMPLOYEE\n'),
    )
    await user.click(await screen.findByRole('button', { name: 'Import users' }))

    expect(getDialog().getByRole('progressbar')).toBeInTheDocument()
    expect(getDialog().getByRole('button', { name: 'Close' })).toBeDisabled()
    expect(getDialog().getByRole('button', { name: 'Choose different file' })).toBeDisabled()
    expect(document.getElementById('bulk-import-file-input')).toBeDisabled()

    resolveImport({ totalRows: 1, imported: 1, failed: 0, errors: [] })
    await waitFor(() => expect(getDialog().getByText('Imported: 1')).toBeInTheDocument())
  })

  it('renders backend validation errors for failed imports', async () => {
    const user = userEvent.setup()
    vi.mocked(usersApi.importUsers).mockResolvedValue({
      totalRows: 3,
      imported: 1,
      failed: 2,
      errors: [
        'Row 3 - Duplicate employeeId EMP002',
        'Row 4 - Duplicate email john.doe@company.com',
        'Row 5 - Invalid role MANAGER',
        'Row 6 - Missing required values',
      ],
    })

    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    await user.upload(
      getFileInput(),
      createCsvFile('Employee ID,Full Name,Email,Role\nEMP010,Jane Doe,jane@example.com,EMPLOYEE\n'),
    )
    await user.click(await screen.findByRole('button', { name: 'Import users' }))

    const dialog = getDialog()
    expect(await dialog.findByText('Row 3 - Duplicate employeeId EMP002')).toBeInTheDocument()
    expect(dialog.getByText('Row 4 - Duplicate email john.doe@company.com')).toBeInTheDocument()
    expect(dialog.getByText('Row 5 - Invalid role MANAGER')).toBeInTheDocument()
    expect(dialog.getByText('Row 6 - Missing required values')).toBeInTheDocument()
    expect(dialog.getByText(/Some rows were imported successfully/)).toBeInTheDocument()
  })

  it('shows invalid role errors for values such as Manager and Administrator', async () => {
    const user = userEvent.setup()
    vi.mocked(usersApi.importUsers).mockResolvedValue({
      totalRows: 2,
      imported: 0,
      failed: 2,
      errors: ['Row 2 - Invalid role Manager', 'Row 3 - Invalid role Administrator'],
    })

    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    await user.upload(
      getFileInput(),
      createCsvFile(
        'Employee ID,Full Name,Email,Role\nEMP010,Jane Doe,jane@example.com,Manager\nEMP011,John Doe,john@example.com,Administrator\n',
      ),
    )
    await user.click(await screen.findByRole('button', { name: 'Import users' }))

    const dialog = getDialog()
    expect(await dialog.findByText('Row 2 - Invalid role Manager')).toBeInTheDocument()
    expect(dialog.getByText('Row 3 - Invalid role Administrator')).toBeInTheDocument()
  })

  it('warns when an empty csv has no data rows', async () => {
    const user = userEvent.setup()
    render(
      <BulkImportDialog
        onClose={onClose}
        onComplete={onComplete}
        onDownloadTemplate={onDownloadTemplate}
        open
      />,
    )

    await user.upload(getFileInput(), createCsvFile(''))

    expect(await getDialog().findByText('No data rows detected.')).toBeInTheDocument()
    expect(getDialog().getByText(/no importable rows/i)).toBeInTheDocument()
  })
})
