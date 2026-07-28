import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { assistantApi } from '../../api/assistantApi'
import { AssistantChatPanel } from './AssistantChatPanel'
import { assistantMessages } from './assistantMessages'

const navigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return {
    ...actual,
    useNavigate: () => navigate,
  }
})

vi.mock('../../api/assistantApi', () => ({
  assistantApi: {
    getConversation: vi.fn(),
    sendMessage: vi.fn(),
  },
}))

function renderPanel(props: { open?: boolean; onClose?: () => void } = {}) {
  return render(
    <MemoryRouter>
      <AssistantChatPanel onClose={props.onClose ?? vi.fn()} open={props.open ?? true} />
    </MemoryRouter>,
  )
}

describe('AssistantChatPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('does not render when closed', () => {
    renderPanel({ open: false })

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('loads and displays an existing conversation when opened', async () => {
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: 'conv-1',
      messages: [
        {
          id: 'msg-1',
          role: 'USER',
          content: 'Show my profile',
          createdAt: '2026-07-01T10:00:00Z',
        },
        {
          id: 'msg-2',
          role: 'ASSISTANT',
          content: 'Here is your profile summary.',
          createdAt: '2026-07-01T10:00:05Z',
        },
      ],
    })

    renderPanel()

    expect(await screen.findByText('Show my profile')).toBeInTheDocument()
    expect(screen.getByText('Here is your profile summary.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Open Projects' })).not.toBeInTheDocument()
    expect(screen.queryByText(assistantMessages.emptyHint)).not.toBeInTheDocument()
  })

  it('shows suggested prompts for an empty conversation', async () => {
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })

    renderPanel()

    expect(await screen.findByText(assistantMessages.emptyHint)).toBeInTheDocument()
    for (const prompt of assistantMessages.suggestedPrompts) {
      expect(screen.getByRole('button', { name: prompt })).toBeInTheDocument()
    }
  })

  it('sends a suggested prompt when clicked', async () => {
    const user = userEvent.setup()
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })
    vi.mocked(assistantApi.sendMessage).mockResolvedValue({
      response: 'Your rank is #3.',
      conversationId: 'conv-1',
      intentType: 'TOOL',
      toolUsed: 'my-leaderboard-rank',
      sources: [],
      metadata: null,
    })

    renderPanel()

    await user.click(await screen.findByRole('button', { name: 'My leaderboard rank' }))

    await waitFor(() => {
      expect(assistantApi.sendMessage).toHaveBeenCalledWith({
        message: 'My leaderboard rank',
        conversationId: null,
      })
    })

    expect(await screen.findByText('Your rank is #3.')).toBeInTheDocument()
  })

  it('sends a message when Enter is pressed', async () => {
    const user = userEvent.setup()
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: 'conv-1',
      messages: [],
    })
    vi.mocked(assistantApi.sendMessage).mockResolvedValue({
      response: 'Spring Boot is a Java framework.',
      conversationId: 'conv-1',
      intentType: 'KNOWLEDGE',
      toolUsed: null,
      sources: [],
      metadata: null,
    })

    renderPanel()

    const input = await screen.findByPlaceholderText(assistantMessages.inputPlaceholder)
    await user.type(input, 'What is Spring Boot?{enter}')

    await waitFor(() => {
      expect(assistantApi.sendMessage).toHaveBeenCalledWith({
        message: 'What is Spring Boot?',
        conversationId: 'conv-1',
      })
    })
  })

  it('shows a retry option when conversation loading fails', async () => {
    vi.mocked(assistantApi.getConversation).mockRejectedValueOnce(new Error('network error'))

    renderPanel()

    expect(await screen.findByText(assistantMessages.loadError)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: assistantMessages.retry })).toBeInTheDocument()
  })

  it('calls onClose when the close button is clicked', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })

    renderPanel({ onClose })

    await user.click(await screen.findByRole('button', { name: assistantMessages.close }))

    expect(onClose).toHaveBeenCalled()
  })

  it('renders a navigation button when chat returns navigation metadata', async () => {
    const user = userEvent.setup()
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })
    vi.mocked(assistantApi.sendMessage).mockResolvedValue({
      response: 'Navigate to Projects.',
      conversationId: 'conv-1',
      intentType: 'NAVIGATION',
      toolUsed: null,
      sources: [],
      metadata: { navigation: { path: '/projects', label: 'Projects' } },
    })

    renderPanel()

    const input = await screen.findByPlaceholderText(assistantMessages.inputPlaceholder)
    await user.type(input, 'Open Projects{enter}')

    expect(await screen.findByText('Navigate to Projects.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Open Projects' })).toBeInTheDocument()
  })

  it('navigates when the navigation button is clicked', async () => {
    const user = userEvent.setup()
    navigate.mockClear()
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })
    vi.mocked(assistantApi.sendMessage).mockResolvedValue({
      response: 'Navigate to Learn.',
      conversationId: 'conv-1',
      intentType: 'NAVIGATION',
      toolUsed: null,
      sources: [],
      metadata: { navigation: { path: '/learn', label: 'Learn' } },
    })

    renderPanel()

    const input = await screen.findByPlaceholderText(assistantMessages.inputPlaceholder)
    await user.type(input, 'Open Learn{enter}')

    await user.click(await screen.findByRole('button', { name: 'Open Learn' }))

    expect(navigate).toHaveBeenCalledWith('/learn')
  })
})
