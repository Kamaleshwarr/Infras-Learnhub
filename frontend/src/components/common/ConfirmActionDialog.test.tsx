import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ConfirmActionDialog } from './ConfirmActionDialog'
import type { UserSummary } from '../../types/users'

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

describe('ConfirmActionDialog', () => {
  it('renders user identity and calls onConfirm', async () => {
    const userEvents = userEvent.setup()
    const onConfirm = vi.fn()
    const onClose = vi.fn()

    render(
      <ConfirmActionDialog
        confirmColor="warning"
        confirmLabel="Deactivate user"
        onClose={onClose}
        onConfirm={onConfirm}
        open
        title="Deactivate user"
        user={user}
      >
        <p>Extra warning</p>
      </ConfirmActionDialog>,
    )

    expect(screen.getByText('Employee User')).toBeInTheDocument()
    expect(screen.getByText('EMP002 · employee@example.com')).toBeInTheDocument()
    expect(screen.getByText('Extra warning')).toBeInTheDocument()

    await userEvents.click(screen.getByRole('button', { name: 'Deactivate user' }))
    expect(onConfirm).toHaveBeenCalledTimes(1)
  })

  it('disables confirm while submitting', () => {
    render(
      <ConfirmActionDialog
        confirmLabel="Activate user"
        onClose={vi.fn()}
        onConfirm={vi.fn()}
        open
        submitting
        title="Activate user"
        user={user}
      />,
    )

    expect(screen.getByRole('progressbar')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeDisabled()
  })
})
