import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { UserSummary } from '../../types/users'
import { UserTable } from './UserTable'

const users: UserSummary[] = [
  {
    active: true,
    createdAtUtc: '2026-06-01T00:00:00Z',
    email: 'admin@example.com',
    employeeId: 'EMP001',
    fullName: 'Admin User',
    id: 'user-1',
    mustChangePassword: false,
    role: 'ADMIN',
    updatedAtUtc: '2026-06-01T00:00:00Z',
  },
  {
    active: false,
    createdAtUtc: '2026-06-02T00:00:00Z',
    email: 'employee@example.com',
    employeeId: 'EMP002',
    fullName: 'Employee User',
    id: 'user-2',
    mustChangePassword: true,
    role: 'EMPLOYEE',
    updatedAtUtc: '2026-06-02T00:00:00Z',
  },
]

const baseProps = {
  hasActiveFilters: false,
  loading: false,
  onActivate: vi.fn(),
  onDeactivate: vi.fn(),
  onEdit: vi.fn(),
  onResetPassword: vi.fn(),
  onSort: vi.fn(),
  showMustChangePasswordColumn: true,
  sort: 'employeeId,asc',
  users,
}

describe('UserTable', () => {
  it('shows activate for inactive users and deactivate for active users', () => {
    render(<UserTable {...baseProps} />)

    expect(screen.getByRole('button', { name: 'Activate user Employee User' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Deactivate user Admin User' })).toBeInTheDocument()
  })

  it('disables self-deactivation for the current admin', () => {
    const onDeactivate = vi.fn()

    render(<UserTable {...baseProps} currentUserId="user-1" onDeactivate={onDeactivate} />)

    const selfDeactivateButton = screen.getByRole('button', { name: 'Deactivate user Admin User' })
    expect(selfDeactivateButton).toBeDisabled()
    expect(onDeactivate).not.toHaveBeenCalled()
  })

  it('calls reset password handler from row action', async () => {
    const user = userEvent.setup()
    const onResetPassword = vi.fn()

    render(<UserTable {...baseProps} onResetPassword={onResetPassword} />)

    await user.click(screen.getByRole('button', { name: 'Reset password for Employee User' }))
    expect(onResetPassword).toHaveBeenCalledWith(users[1])
  })
})
