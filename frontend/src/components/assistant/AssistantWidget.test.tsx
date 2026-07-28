import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AssistantProvider } from '../../assistant/AssistantProvider'
import { assistantApi } from '../../api/assistantApi'
import { AssistantWidget } from './AssistantWidget'
import { assistantMessages } from './assistantMessages'

vi.mock('../../api/assistantApi', () => ({
  assistantApi: {
    getStatus: vi.fn(),
    getConversation: vi.fn(),
    sendMessage: vi.fn(),
  },
}))

describe('AssistantWidget', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders nothing while assistant status is loading', () => {
    vi.mocked(assistantApi.getStatus).mockReturnValue(new Promise(() => undefined))

    render(
      <AssistantProvider>
        <AssistantWidget />
      </AssistantProvider>,
    )

    expect(screen.queryByRole('button', { name: assistantMessages.open })).not.toBeInTheDocument()
  })

  it('renders nothing when assistant is disabled', async () => {
    vi.mocked(assistantApi.getStatus).mockResolvedValue({
      enabled: false,
      llmProvider: 'mock',
      llmHealthy: true,
    })

    render(
      <AssistantProvider>
        <AssistantWidget />
      </AssistantProvider>,
    )

    await waitFor(() => {
      expect(assistantApi.getStatus).toHaveBeenCalled()
    })

    expect(screen.queryByRole('button', { name: assistantMessages.open })).not.toBeInTheDocument()
  })

  it('renders the floating action button when assistant is enabled', async () => {
    vi.mocked(assistantApi.getStatus).mockResolvedValue({
      enabled: true,
      llmProvider: 'mock',
      llmHealthy: true,
    })

    render(
      <AssistantProvider>
        <AssistantWidget />
      </AssistantProvider>,
    )

    expect(await screen.findByRole('button', { name: assistantMessages.open })).toBeInTheDocument()
  })

  it('opens the chat panel when the floating button is clicked', async () => {
    const user = userEvent.setup()
    vi.mocked(assistantApi.getStatus).mockResolvedValue({
      enabled: true,
      llmProvider: 'mock',
      llmHealthy: true,
    })
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })

    render(
      <AssistantProvider>
        <AssistantWidget />
      </AssistantProvider>,
    )

    await user.click(await screen.findByRole('button', { name: assistantMessages.open }))

    expect(await screen.findByRole('dialog', { name: assistantMessages.title })).toBeInTheDocument()
    expect(assistantApi.getConversation).toHaveBeenCalled()
  })
})
