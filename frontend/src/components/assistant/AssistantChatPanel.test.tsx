import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { assistantApi } from '../../api/assistantApi'
import { AssistantChatPanel } from './AssistantChatPanel'
import { assistantMessages } from './assistantMessages'

vi.mock('../../api/assistantApi', () => ({
  assistantApi: {
    getConversation: vi.fn(),
    sendMessage: vi.fn(),
  },
}))

describe('AssistantChatPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('does not render when closed', () => {
    render(<AssistantChatPanel onClose={vi.fn()} open={false} />)

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

    render(<AssistantChatPanel onClose={vi.fn()} open />)

    expect(await screen.findByText('Show my profile')).toBeInTheDocument()
    expect(screen.getByText('Here is your profile summary.')).toBeInTheDocument()
    expect(screen.queryByText(assistantMessages.emptyHint)).not.toBeInTheDocument()
  })

  it('shows suggested prompts for an empty conversation', async () => {
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })

    render(<AssistantChatPanel onClose={vi.fn()} open />)

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

    render(<AssistantChatPanel onClose={vi.fn()} open />)

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

    render(<AssistantChatPanel onClose={vi.fn()} open />)

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

    render(<AssistantChatPanel onClose={vi.fn()} open />)

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

    render(<AssistantChatPanel onClose={onClose} open />)

    await user.click(await screen.findByRole('button', { name: assistantMessages.close }))

    expect(onClose).toHaveBeenCalled()
  })
})
