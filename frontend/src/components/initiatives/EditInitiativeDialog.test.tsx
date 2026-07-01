import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import type { Initiative } from '../../types/initiatives'
import { EditInitiativeDialog } from './EditInitiativeDialog'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    update: vi.fn(),
  },
}))

const frozenNow = Date.parse('2026-06-27T12:00:00.000Z')

const initiative: Initiative = {
  createdAtUtc: '2025-01-01T10:00:00Z',
  createdBy: {
    email: 'admin@example.com',
    employeeId: 'ADMIN001',
    fullName: 'Admin User',
    id: 'admin-1',
  },
  description: 'Azure certification program',
  expiryDateUtc: '2026-12-31T00:00:00.000Z',
  id: 'initiative-1',
  rewardDescription: '$500 credit',
  startDateUtc: '2026-07-01T00:00:00.000Z',
  status: 'ACTIVE',
  title: 'Azure Certification',
  updatedAtUtc: '2026-06-01T12:00:00Z',
}

function getEditDialog() {
  return within(screen.getByRole('dialog', { name: 'Edit Initiative' }))
}

describe('EditInitiativeDialog', () => {
  const onClose = vi.fn()
  const onSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.setSystemTime(frozenNow)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('pre-populates existing initiative values', () => {
    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    expect(dialog.getByLabelText(/^Title/i)).toHaveValue('Azure Certification')
    expect(dialog.getByLabelText(/^Description/i)).toHaveValue('Azure certification program')
    expect(dialog.getByLabelText(/^Reward \/ Benefits/i)).toHaveValue('$500 credit')
    expect(dialog.getByLabelText(/^Start date \(UTC\)/i)).toHaveValue('2026-07-01')
    expect(dialog.getByLabelText(/^Expiry date \(UTC\)/i)).toHaveValue('2026-12-31')
    expect(dialog.getByRole('combobox', { name: /^Status/i })).toHaveTextContent('Active')
  })

  it('shows metadata and edit-only status helper text', () => {
    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    expect(dialog.getByText(/Created by: Admin User/i)).toBeInTheDocument()
    expect(dialog.getByText(/Created on:/i)).toBeInTheDocument()
    expect(dialog.getByText(/Last updated:/i)).toBeInTheDocument()
    expect(
      dialog.getByText(/Expired initiatives can be reactivated to Active through Edit/i),
    ).toBeInTheDocument()
  })

  it('keeps save enabled and shows validation on submit', async () => {
    const user = userEvent.setup({ delay: null })
    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    await user.clear(dialog.getByLabelText(/^Title/i))
    await user.clear(dialog.getByLabelText(/^Description/i))

    expect(dialog.getByRole('button', { name: 'Save' })).toBeEnabled()
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    expect(dialog.getByText(/Title is required/i)).toBeInTheDocument()
    expect(dialog.getByText(/Description is required/i)).toBeInTheDocument()
    expect(initiativesApi.update).not.toHaveBeenCalled()
  })

  it('rejects modified start dates before today in edit mode', async () => {
    const user = userEvent.setup({ delay: null })
    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    fireEvent.change(dialog.getByLabelText(/^Start date \(UTC\)/i), { target: { value: '2026-06-26' } })
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    expect(dialog.getByText(/Start date cannot be earlier than today/i)).toBeInTheDocument()
    expect(initiativesApi.update).not.toHaveBeenCalled()
  })

  it('allows editing other fields when start date is unchanged and in the past', async () => {
    const user = userEvent.setup({ delay: null })
    vi.setSystemTime(Date.parse('2026-07-20T12:00:00.000Z'))
    const pastStartInitiative: Initiative = {
      ...initiative,
      startDateUtc: '2026-07-01T00:00:00.000Z',
    }
    vi.mocked(initiativesApi.update).mockResolvedValue({
      ...pastStartInitiative,
      description: 'Updated certification program',
    })

    render(
      <EditInitiativeDialog
        initiative={pastStartInitiative}
        onClose={onClose}
        onSuccess={onSuccess}
        open
      />,
    )
    const dialog = getEditDialog()

    await user.clear(dialog.getByLabelText(/^Description/i))
    await user.type(dialog.getByLabelText(/^Description/i), 'Updated certification program')
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(initiativesApi.update).toHaveBeenCalledWith('initiative-1', {
        title: 'Azure Certification',
        description: 'Updated certification program',
        rewardDescription: '$500 credit',
        startDateUtc: '2026-07-01T00:00:00.000Z',
        expiryDateUtc: '2026-12-31T00:00:00.000Z',
        status: 'ACTIVE',
      }),
    )
    expect(onSuccess).toHaveBeenCalledTimes(1)
  })

  it('allows modified start dates on or after today in edit mode', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(initiativesApi.update).mockResolvedValue(initiative)

    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    fireEvent.change(dialog.getByLabelText(/^Start date \(UTC\)/i), { target: { value: '2026-06-27' } })
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    await waitFor(() => expect(initiativesApi.update).toHaveBeenCalled())
    expect(onSuccess).toHaveBeenCalledTimes(1)
  })

  it('shows expiry before start validation in edit mode', async () => {
    const user = userEvent.setup({ delay: null })
    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    fireEvent.change(dialog.getByLabelText(/^Expiry date \(UTC\)/i), { target: { value: '2026-06-20' } })
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    expect(dialog.getByText(/Expiry date must be on or after the start date/i)).toBeInTheDocument()
    expect(initiativesApi.update).not.toHaveBeenCalled()
  })

  it('sets expiry to today when status changes to expired', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(initiativesApi.update).mockResolvedValue({
      ...initiative,
      expiryDateUtc: '2026-06-27T00:00:00.000Z',
      startDateUtc: '2026-06-27T00:00:00.000Z',
      status: 'EXPIRED',
    })

    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    await user.click(dialog.getByRole('combobox', { name: /^Status/i }))
    await user.click(screen.getByRole('option', { name: 'Expired' }))

    expect(dialog.getByLabelText(/^Expiry date \(UTC\)/i)).toHaveValue('2026-06-27')
    expect(dialog.getByLabelText(/^Start date \(UTC\)/i)).toHaveValue('2026-06-27')

    await user.click(dialog.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(initiativesApi.update).toHaveBeenCalledWith('initiative-1', {
        title: 'Azure Certification',
        description: 'Azure certification program',
        rewardDescription: '$500 credit',
        startDateUtc: '2026-06-27T00:00:00.000Z',
        expiryDateUtc: '2026-06-27T00:00:00.000Z',
        status: 'EXPIRED',
      }),
    )
  })

  it('updates an initiative and calls onSuccess', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(initiativesApi.update).mockResolvedValue({
      ...initiative,
      title: 'Updated Azure Certification',
    })

    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    await user.clear(dialog.getByLabelText(/^Title/i))
    await user.type(dialog.getByLabelText(/^Title/i), 'Updated Azure Certification')
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(initiativesApi.update).toHaveBeenCalledWith('initiative-1', {
        title: 'Updated Azure Certification',
        description: 'Azure certification program',
        rewardDescription: '$500 credit',
        startDateUtc: '2026-07-01T00:00:00.000Z',
        expiryDateUtc: '2026-12-31T00:00:00.000Z',
        status: 'ACTIVE',
      }),
    )
    expect(onSuccess).toHaveBeenCalledTimes(1)
    expect(onClose).not.toHaveBeenCalled()
  })

  it('shows server validation errors and preserves edits', async () => {
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
    vi.mocked(initiativesApi.update).mockRejectedValue(error)

    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    await user.clear(dialog.getByLabelText(/^Title/i))
    await user.type(dialog.getByLabelText(/^Title/i), 'Updated title')
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    expect(await dialog.findByText('Validation failed')).toBeInTheDocument()
    expect(dialog.getByText('must not be blank')).toBeInTheDocument()
    expect(dialog.getByLabelText(/^Title/i)).toHaveValue('Updated title')
    expect(onSuccess).not.toHaveBeenCalled()
  })

  it('prompts before discarding unsaved changes', async () => {
    const user = userEvent.setup({ delay: null })
    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getEditDialog()

    await user.clear(dialog.getByLabelText(/^Title/i))
    await user.type(dialog.getByLabelText(/^Title/i), 'Changed title')
    await user.click(dialog.getByRole('button', { name: 'Cancel' }))

    expect(screen.getByRole('dialog', { name: 'Discard unsaved changes?' })).toBeInTheDocument()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('closes without discard prompt when the form is unchanged', async () => {
    const user = userEvent.setup({ delay: null })
    render(<EditInitiativeDialog initiative={initiative} onClose={onClose} onSuccess={onSuccess} open />)

    await user.click(getEditDialog().getByRole('button', { name: 'Cancel' }))

    expect(onClose).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog', { name: 'Discard unsaved changes?' })).not.toBeInTheDocument()
  })
})
