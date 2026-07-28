import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AssistantMessageBubble } from './AssistantMessageBubble'

const navigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return {
    ...actual,
    useNavigate: () => navigate,
  }
})

function renderBubble(message: Parameters<typeof AssistantMessageBubble>[0]['message']) {
  return render(
    <MemoryRouter>
      <AssistantMessageBubble message={message} />
    </MemoryRouter>,
  )
}

describe('AssistantMessageBubble', () => {
  it('renders assistant text without a navigation button when metadata is absent', () => {
    renderBubble({
      id: 'msg-1',
      role: 'ASSISTANT',
      content: 'Your rank is #3.',
      createdAt: '2026-07-01T10:00:00Z',
    })

    expect(screen.getByText('Your rank is #3.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Open Projects' })).not.toBeInTheDocument()
  })

  it('renders a navigation button for assistant messages with navigation metadata', () => {
    renderBubble({
      id: 'msg-2',
      role: 'ASSISTANT',
      content: 'Navigate to Projects.',
      createdAt: '2026-07-01T10:00:05Z',
      navigation: { path: '/projects', label: 'Projects' },
    })

    expect(screen.getByText('Navigate to Projects.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Open Projects' })).toBeInTheDocument()
  })

  it('navigates when the navigation button is clicked', async () => {
    const user = userEvent.setup()
    navigate.mockClear()

    renderBubble({
      id: 'msg-3',
      role: 'ASSISTANT',
      content: 'Navigate to Learn.',
      createdAt: '2026-07-01T10:00:10Z',
      navigation: { path: '/learn', label: 'Learn' },
    })

    await user.click(screen.getByRole('button', { name: 'Open Learn' }))

    expect(navigate).toHaveBeenCalledWith('/learn')
  })

  it('does not render navigation buttons on user messages', () => {
    renderBubble({
      id: 'msg-4',
      role: 'USER',
      content: 'Open Projects',
      createdAt: '2026-07-01T10:00:00Z',
      navigation: { path: '/projects', label: 'Projects' },
    })

    expect(screen.getByText('Open Projects')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Open Projects' })).not.toBeInTheDocument()
  })

  it('supports navigation targets across assistant routes', () => {
    const targets = [
      { path: '/projects', label: 'Projects' },
      { path: '/', label: 'Dashboard' },
      { path: '/learn', label: 'Learn' },
      { path: '/leaderboards/global', label: 'Leaderboards' },
    ]

    for (const navigation of targets) {
      const { unmount } = renderBubble({
        id: `msg-${navigation.path}`,
        role: 'ASSISTANT',
        content: `Navigate to ${navigation.label}.`,
        createdAt: '2026-07-01T10:00:00Z',
        navigation,
      })

      expect(screen.getByRole('button', { name: `Open ${navigation.label}` })).toBeInTheDocument()
      unmount()
    }
  })
})
