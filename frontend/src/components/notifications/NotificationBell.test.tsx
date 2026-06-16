import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { notificationsApi } from '../../api/notificationsApi'
import { NotificationBell } from './NotificationBell'

vi.mock('../../api/notificationsApi', () => ({
  notificationsApi: {
    unreadCount: vi.fn(),
    list: vi.fn(),
    markRead: vi.fn(),
    markAllRead: vi.fn(),
  },
}))

describe('NotificationBell', () => {
  beforeEach(() => {
    vi.mocked(notificationsApi.unreadCount).mockResolvedValue({ count: 3 })
    vi.mocked(notificationsApi.list).mockResolvedValue({
      content: [
        {
          id: 'notification-1',
          type: 'ACCOUNT_CREATED',
          title: 'Welcome to Learning Hub',
          message: 'Your account has been created.',
          entityType: 'USER',
          entityId: 'user-1',
          actionPath: '/change-password',
          read: false,
          createdAtUtc: '2026-06-16T10:00:00.000Z',
        },
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      sort: [{ property: 'createdAt', direction: 'DESC' }],
    })
  })

  it('renders unread badge count', async () => {
    render(
      <MemoryRouter>
        <NotificationBell />
      </MemoryRouter>,
    )

    expect(await screen.findByLabelText('Notifications')).toBeInTheDocument()
    expect(await screen.findByText('3')).toBeInTheDocument()
  })

  it('opens dropdown with notification preview', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <NotificationBell />
      </MemoryRouter>,
    )

    await user.click(await screen.findByLabelText('Notifications'))

    expect(await screen.findByText('Welcome to Learning Hub')).toBeInTheDocument()
    expect(screen.getByText('View all notifications')).toBeInTheDocument()
  })

  it('marks all notifications read from dropdown', async () => {
    const user = userEvent.setup()
    vi.mocked(notificationsApi.markAllRead).mockResolvedValue()
    vi.mocked(notificationsApi.unreadCount)
      .mockResolvedValueOnce({ count: 2 })
      .mockResolvedValue({ count: 0 })

    render(
      <MemoryRouter>
        <NotificationBell />
      </MemoryRouter>,
    )

    await user.click(await screen.findByLabelText('Notifications'))
    await user.click(screen.getByRole('button', { name: 'Mark all read' }))

    await waitFor(() => {
      expect(notificationsApi.markAllRead).toHaveBeenCalled()
    })
  })
})
