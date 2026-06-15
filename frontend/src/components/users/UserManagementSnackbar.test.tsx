import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { UserManagementSnackbar } from './UserManagementSnackbar'

describe('UserManagementSnackbar', () => {
  it('renders success notification content', () => {
    render(
      <UserManagementSnackbar
        notification={{ message: 'User created successfully.', severity: 'success' }}
        onClose={vi.fn()}
      />,
    )

    expect(screen.getByText('User created successfully.')).toBeInTheDocument()
  })

  it('ignores clickaway close events', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()

    render(
      <UserManagementSnackbar
        notification={{ message: 'User updated successfully.', severity: 'success' }}
        onClose={onClose}
      />,
    )

    await user.click(document.body)
    expect(onClose).not.toHaveBeenCalled()
  })
})
