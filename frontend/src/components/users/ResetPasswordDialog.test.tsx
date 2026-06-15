import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { usersApi } from '../../api/usersApi'
import type { UserSummary } from '../../types/users'
import { ResetPasswordDialog } from './ResetPasswordDialog'

vi.mock('../../api/usersApi', () => ({
  usersApi: {
    resetPassword: vi.fn(),
  },
}))

const user: UserSummary = {
  active: true,
  createdAtUtc: '2026-06-01T00:00:00Z',
  email: 'employee@example.com',
  employeeId: 'EMP002',
  fullName: 'Employee User',
  id: 'user-2',
  mustChangePassword: false,
  role: 'EMPLOYEE',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

function getDialog() {
  return within(screen.getByRole('dialog'))
}

describe('ResetPasswordDialog', () => {
  const onClose = vi.fn()
  const onSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('requires confirmation before password entry', async () => {
    const userEvents = userEvent.setup()
    render(<ResetPasswordDialog onClose={onClose} onSuccess={onSuccess} open user={user} />)

    expect(
      screen.getByText('The user will be required to change this password on next sign-in.'),
    ).toBeInTheDocument()
    expect(screen.queryByLabelText(/^New Password/)).not.toBeInTheDocument()

    await userEvents.click(screen.getByRole('button', { name: 'Continue' }))

    expect(getDialog().getByLabelText(/^New Password/)).toBeInTheDocument()
    expect(
      getDialog().getByText('User will be required to change their password at next sign-in.'),
    ).toBeInTheDocument()
  })

  it('submits a valid password reset', async () => {
    const userEvents = userEvent.setup()
    vi.mocked(usersApi.resetPassword).mockResolvedValue()

    render(<ResetPasswordDialog onClose={onClose} onSuccess={onSuccess} open user={user} />)

    await userEvents.click(screen.getByRole('button', { name: 'Continue' }))
    const dialog = getDialog()
    await userEvents.type(dialog.getByLabelText(/^New Password/), 'Temp@12345')
    await userEvents.type(dialog.getByLabelText(/^Confirm Password/), 'Temp@12345')
    await userEvents.click(dialog.getByRole('button', { name: 'Reset password' }))

    expect(usersApi.resetPassword).toHaveBeenCalledWith('user-2', { password: 'Temp@12345' })
    expect(onSuccess).toHaveBeenCalledTimes(1)
  })
})
