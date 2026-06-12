import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { usersApi } from '../../api/usersApi'
import type { UserSummary } from '../../types/users'
import { EditUserDialog } from './EditUserDialog'

vi.mock('../../api/usersApi', () => ({
  usersApi: {
    get: vi.fn(),
    update: vi.fn(),
  },
}))

const adminUser: UserSummary = {
  active: true,
  createdAtUtc: '2026-06-01T00:00:00Z',
  email: 'admin@example.com',
  employeeId: 'EMP001',
  fullName: 'Admin User',
  id: 'user-1',
  role: 'ADMIN',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

function getDialog() {
  return within(screen.getByRole('dialog'))
}

describe('EditUserDialog', () => {
  const onClose = vi.fn()
  const onSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(usersApi.get).mockResolvedValue(adminUser)
  })

  it('loads fresh user details on open', async () => {
    render(
      <EditUserDialog
        currentUserId="user-2"
        onClose={onClose}
        onSuccess={onSuccess}
        open
        user={adminUser}
      />,
    )

    await waitFor(() => expect(usersApi.get).toHaveBeenCalledWith('user-1'))
    const dialog = getDialog()
    expect(dialog.getByRole('textbox', { name: 'Employee ID' })).toHaveValue('EMP001')
    expect(dialog.getByRole('textbox', { name: 'Full Name' })).toHaveValue('Admin User')
  })

  it('disables role changes when editing the current admin', async () => {
    render(
      <EditUserDialog
        currentUserId="user-1"
        onClose={onClose}
        onSuccess={onSuccess}
        open
        user={adminUser}
      />,
    )

    const dialog = getDialog()
    expect(
      await dialog.findByText(
        'You cannot change your own role. Ask another administrator if a role change is required.',
      ),
    ).toBeInTheDocument()
    expect(dialog.getByRole('combobox', { name: 'Role' })).toHaveAttribute('aria-disabled', 'true')
  })

  it('updates a user with normalized email', async () => {
    const user = userEvent.setup()
    vi.mocked(usersApi.update).mockResolvedValue({
      ...adminUser,
      fullName: 'Updated Admin',
      email: 'updated@example.com',
    })

    render(
      <EditUserDialog
        currentUserId="user-2"
        onClose={onClose}
        onSuccess={onSuccess}
        open
        user={adminUser}
      />,
    )

    const dialog = getDialog()
    await dialog.findByRole('textbox', { name: 'Full Name' })
    await user.clear(dialog.getByRole('textbox', { name: 'Full Name' }))
    await user.type(dialog.getByRole('textbox', { name: 'Full Name' }), 'Updated Admin')
    await user.clear(dialog.getByRole('textbox', { name: 'Email' }))
    await user.type(dialog.getByRole('textbox', { name: 'Email' }), '  Updated@Example.COM ')
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(usersApi.update).toHaveBeenCalledWith('user-1', {
        fullName: 'Updated Admin',
        email: 'updated@example.com',
        role: 'ADMIN',
      }),
    )
    expect(onSuccess).toHaveBeenCalledTimes(1)
    expect(onClose).toHaveBeenCalledTimes(1)
  })
})
