import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import { CreateInitiativeDialog } from './CreateInitiativeDialog'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    create: vi.fn(),
  },
}))

const createdInitiative = {
  createdAtUtc: '2026-06-19T00:00:00Z',
  createdBy: {
    email: 'admin@example.com',
    employeeId: 'ADMIN001',
    fullName: 'Admin User',
    id: 'admin-1',
  },
  description: 'Azure certification program',
  expiryDateUtc: '2026-12-31T00:00:00.000Z',
  id: 'initiative-new',
  rewardDescription: '$500 credit',
  startDateUtc: '2026-06-01T00:00:00.000Z',
  status: 'DRAFT' as const,
  title: 'Azure Certification',
  updatedAtUtc: '2026-06-19T00:00:00Z',
}

function getCreateDialog() {
  return within(screen.getByRole('dialog', { name: 'Create Initiative' }))
}

describe('CreateInitiativeDialog', () => {
  const onClose = vi.fn()
  const onSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('defaults status to draft', () => {
    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)

    expect(getCreateDialog().getByRole('combobox', { name: /^Status/i })).toHaveTextContent('Draft')
  })

  it('keeps create enabled and shows validation on submit', async () => {
    const user = userEvent.setup({ delay: null })
    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getCreateDialog()

    expect(dialog.getByRole('button', { name: 'Create' })).toBeEnabled()
    await user.click(dialog.getByRole('button', { name: 'Create' }))

    expect(dialog.getByText(/Title is required/i)).toBeInTheDocument()
    expect(dialog.getByText(/Description is required/i)).toBeInTheDocument()
    expect(initiativesApi.create).not.toHaveBeenCalled()
  })

  it('shows date validation errors on submit', async () => {
    const user = userEvent.setup({ delay: null })
    vi.setSystemTime(new Date('2026-06-19T12:00:00.000Z'))

    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getCreateDialog()

    await user.type(dialog.getByLabelText(/^Title/i), 'Azure Certification')
    await user.type(dialog.getByLabelText(/^Description/i), 'Azure certification program')
    fireEvent.change(dialog.getByLabelText(/^Start date \(UTC\)/i), { target: { value: '2026-06-18' } })
    fireEvent.change(dialog.getByLabelText(/^Expiry date \(UTC\)/i), { target: { value: '2026-06-17' } })
    await user.click(dialog.getByRole('button', { name: 'Create' }))

    expect(dialog.getByText(/Start date cannot be earlier than today/i)).toBeInTheDocument()
    expect(dialog.getByText(/Expiry date must be on or after the start date/i)).toBeInTheDocument()
    expect(initiativesApi.create).not.toHaveBeenCalled()

    vi.useRealTimers()
  })

  it('does not show edit-only status helper text', () => {
    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)

    expect(
      screen.queryByText(/Expired initiatives can be reactivated to Active through Edit/i),
    ).not.toBeInTheDocument()
  })

  it('creates an initiative and calls onSuccess', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(initiativesApi.create).mockResolvedValue(createdInitiative)

    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getCreateDialog()

    await user.type(dialog.getByLabelText(/^Title/i), 'Azure Certification')
    await user.type(dialog.getByLabelText(/^Description/i), 'Azure certification program')
    await user.type(dialog.getByLabelText(/^Reward \/ Benefits/i), '$500 credit')
    await user.click(dialog.getByRole('button', { name: 'Create' }))

    await waitFor(() =>
      expect(initiativesApi.create).toHaveBeenCalledWith({
        title: 'Azure Certification',
        description: 'Azure certification program',
        rewardDescription: '$500 credit',
        startDateUtc: expect.stringMatching(/T00:00:00\.000Z$/),
        expiryDateUtc: expect.stringMatching(/T00:00:00\.000Z$/),
        status: 'DRAFT',
      }),
    )
    expect(onSuccess).toHaveBeenCalledTimes(1)
    expect(onClose).not.toHaveBeenCalled()
  })

  it('shows client validation errors when required fields are missing', async () => {
    const user = userEvent.setup({ delay: null })
    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getCreateDialog()

    await user.click(dialog.getByRole('button', { name: 'Create' }))

    expect(dialog.getByText(/Title is required/i)).toBeInTheDocument()
    expect(dialog.getByText(/Description is required/i)).toBeInTheDocument()
    expect(initiativesApi.create).not.toHaveBeenCalled()
  })

  it('shows server validation errors', async () => {
    const user = userEvent.setup({ delay: null })
    const error = new axios.AxiosError(
      'Request failed',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        data: {
          message: 'Validation failed',
          validationErrors: {
            title: 'must not be blank',
          },
        },
        headers: {},
        status: 400,
        statusText: 'Bad Request',
        config: { headers: new axios.AxiosHeaders() },
      },
    )
    vi.mocked(initiativesApi.create).mockRejectedValue(error)

    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getCreateDialog()

    await user.type(dialog.getByLabelText(/^Title/i), 'Azure Certification')
    await user.type(dialog.getByLabelText(/^Description/i), 'Azure certification program')
    await user.click(dialog.getByRole('button', { name: 'Create' }))

    expect(await dialog.findByText('Validation failed')).toBeInTheDocument()
    expect(dialog.getByText('must not be blank')).toBeInTheDocument()
    expect(onSuccess).not.toHaveBeenCalled()
  })

  it('prompts before discarding unsaved changes', async () => {
    const user = userEvent.setup({ delay: null })
    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getCreateDialog()

    await user.type(dialog.getByLabelText(/^Title/i), 'Draft Initiative')
    await user.click(dialog.getByRole('button', { name: 'Cancel' }))

    expect(screen.getByRole('dialog', { name: 'Discard unsaved changes?' })).toBeInTheDocument()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('closes without discard prompt when the form is unchanged', async () => {
    const user = userEvent.setup({ delay: null })
    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)

    await user.click(getCreateDialog().getByRole('button', { name: 'Cancel' }))

    expect(onClose).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog', { name: 'Discard unsaved changes?' })).not.toBeInTheDocument()
  })

  it('discards unsaved changes after confirmation', async () => {
    const user = userEvent.setup({ delay: null })
    render(<CreateInitiativeDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getCreateDialog()

    await user.type(dialog.getByLabelText(/^Title/i), 'Draft Initiative')
    await user.click(dialog.getByRole('button', { name: 'Cancel' }))
    await user.click(screen.getByRole('button', { name: 'Discard' }))

    expect(onClose).toHaveBeenCalledTimes(1)
  })
})
