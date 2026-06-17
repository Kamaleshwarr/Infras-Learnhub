import type { ReactNode } from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { notificationsApi } from '../../api/notificationsApi'
import { NotificationBell } from '../../components/notifications/NotificationBell'
import { NotificationProvider } from '../../notifications/NotificationProvider'
import { NotificationsPage } from './NotificationsPage'

vi.mock('../../api/notificationsApi', () => ({
  notificationsApi: {
    list: vi.fn(),
    unreadCount: vi.fn(),
    markRead: vi.fn(),
    markAllRead: vi.fn(),
  },
}))

const pageResponse = {
  content: [
    {
      id: 'notification-1',
      type: 'CERTIFICATE_APPROVED' as const,
      title: 'Certificate approved',
      message: 'Your certificate submission was approved.',
      entityType: 'CERTIFICATE_SUBMISSION' as const,
      entityId: 'submission-1',
      actionPath: '/submissions',
      read: false,
      createdAtUtc: '2026-06-16T10:00:00.000Z',
    },
  ],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
  sort: [{ property: 'createdAt', direction: 'DESC' as const }],
}

function renderWithProvider(ui: ReactNode) {
  return render(
    <MemoryRouter>
      <NotificationProvider>{ui}</NotificationProvider>
    </MemoryRouter>,
  )
}

function renderNotificationsWithBell() {
  return renderWithProvider(
    <>
      <NotificationBell />
      <NotificationsPage />
    </>,
  )
}

describe('NotificationsPage', () => {
  beforeEach(() => {
    vi.mocked(notificationsApi.list).mockResolvedValue(pageResponse)
    vi.mocked(notificationsApi.markAllRead).mockResolvedValue()
    vi.mocked(notificationsApi.unreadCount).mockResolvedValue({ count: 0 })
  })

  it('renders notification list', async () => {
    renderWithProvider(<NotificationsPage />)

    expect(await screen.findByText('Certificate approved')).toBeInTheDocument()
    expect(screen.getByText('Your certificate submission was approved.')).toBeInTheDocument()
  })

  it('shows empty unread state', async () => {
    const user = userEvent.setup()
    vi.mocked(notificationsApi.list).mockResolvedValue({
      ...pageResponse,
      content: [],
      totalElements: 0,
    })

    renderWithProvider(<NotificationsPage />)

    const unreadTab = await screen.findByRole('tab', { name: 'Unread' })
    await user.click(unreadTab)

    expect(await screen.findByText('No unread notifications.')).toBeInTheDocument()
  })

  it('marks all notifications as read', async () => {
    const user = userEvent.setup()

    renderWithProvider(<NotificationsPage />)

    await screen.findByText('Certificate approved')
    await user.click(screen.getByRole('button', { name: 'Mark all as read' }))

    await waitFor(() => {
      expect(notificationsApi.markAllRead).toHaveBeenCalled()
    })
  })

  it('updates bell badge after mark all read on notifications page', async () => {
    const user = userEvent.setup()
    vi.mocked(notificationsApi.unreadCount)
      .mockResolvedValueOnce({ count: 2 })
      .mockResolvedValue({ count: 0 })

    renderNotificationsWithBell()

    expect(await screen.findByText('2')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Mark all as read' }))

    await waitFor(() => {
      expect(notificationsApi.markAllRead).toHaveBeenCalled()
      expect(screen.queryByText('2')).not.toBeInTheDocument()
    })
  })
})
