import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { UserListToolbar } from './UserListToolbar'

describe('UserListToolbar', () => {
  it('renders import and template actions', async () => {
    const user = userEvent.setup()
    const onCreateUser = vi.fn()
    const onImportUsers = vi.fn()
    const onDownloadTemplate = vi.fn()

    render(
      <UserListToolbar
        onCreateUser={onCreateUser}
        onDownloadTemplate={onDownloadTemplate}
        onImportUsers={onImportUsers}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Download Template' }))
    await user.click(screen.getByRole('button', { name: 'Import Users' }))
    await user.click(screen.getByRole('button', { name: 'Create User' }))

    expect(onDownloadTemplate).toHaveBeenCalledTimes(1)
    expect(onImportUsers).toHaveBeenCalledTimes(1)
    expect(onCreateUser).toHaveBeenCalledTimes(1)
  })
})
