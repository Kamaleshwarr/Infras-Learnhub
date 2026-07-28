import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { assistantApi } from '../../api/assistantApi'
import { AssistantWidget } from './AssistantWidget'

vi.mock('../../api/assistantApi', () => ({
  assistantApi: {
    getStatus: vi.fn(),
    getConversation: vi.fn(),
    sendMessage: vi.fn(),
  },
}))

describe('AssistantWidget', () => {
  async function waitForAssistantButton() {
    const button = await screen.findByRole('button', { name: 'Open AI Assistant' })
    await waitFor(() => expect(button).not.toBeDisabled())
    return button
  }

  beforeEach(() => {
    vi.mocked(assistantApi.getStatus).mockResolvedValue({
      enabled: true,
      llmProvider: 'mock',
      llmHealthy: true,
    })
    vi.mocked(assistantApi.getConversation).mockResolvedValue({
      conversationId: null,
      messages: [],
    })
  })

  it('renders disabled assistant button when feature is off', async () => {
    vi.mocked(assistantApi.getStatus).mockResolvedValue({
      enabled: false,
      llmProvider: 'mock',
      llmHealthy: true,
    })

    render(
      <MemoryRouter>
        <AssistantWidget />
      </MemoryRouter>,
    )

    const button = await screen.findByRole('button', { name: 'Open AI Assistant' })
    await waitFor(() => expect(button).toBeDisabled())
  })

  it('opens chat panel and shows empty state', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <AssistantWidget />
      </MemoryRouter>,
    )

    await user.click(await waitForAssistantButton())

    expect(await screen.findByText('How can I help?')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Ask a question…')).toBeInTheDocument()
  })

  it('sends a message and renders assistant response', async () => {
    const user = userEvent.setup()
    vi.mocked(assistantApi.sendMessage).mockResolvedValue({
      response: 'Docker is a container platform.',
      conversationId: 'conversation-1',
      intentType: 'KNOWLEDGE',
      toolUsed: null,
      sources: [{ serviceName: 'mock', toolName: null, confidence: 'LOW' }],
      metadata: { llmProvider: 'mock' },
    })

    render(
      <MemoryRouter>
        <AssistantWidget />
      </MemoryRouter>,
    )

    await user.click(await waitForAssistantButton())
    await user.type(screen.getByPlaceholderText('Ask a question…'), 'what is docker')
    await user.click(screen.getByLabelText('Send message'))

    expect(await screen.findByText('what is docker')).toBeInTheDocument()
    await waitFor(() => {
      expect(assistantApi.sendMessage).toHaveBeenCalledWith({
        message: 'what is docker',
        conversationId: null,
      })
      expect(screen.getByText('Docker is a container platform.')).toBeInTheDocument()
    })
  })

  it('shows navigation action for navigation responses', async () => {
    const user = userEvent.setup()
    vi.mocked(assistantApi.sendMessage).mockResolvedValue({
      response: 'Navigate to Projects.',
      conversationId: 'conversation-1',
      intentType: 'NAVIGATION',
      toolUsed: null,
      sources: [],
      metadata: { navigation: { path: '/projects', label: 'Projects' } },
    })

    render(
      <MemoryRouter>
        <AssistantWidget />
      </MemoryRouter>,
    )

    await user.click(await waitForAssistantButton())
    await user.click(screen.getByText('Open Learn'))

    expect(await screen.findByRole('button', { name: 'Go to Projects' })).toBeInTheDocument()
  })
})
