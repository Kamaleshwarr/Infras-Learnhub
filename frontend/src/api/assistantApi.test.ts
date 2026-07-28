import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { assistantApi } from './assistantApi'

vi.mock('./httpClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('assistantApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetches assistant status', async () => {
    const status = {
      enabled: true,
      llmProvider: 'mock',
      llmHealthy: true,
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: status })

    const result = await assistantApi.getStatus()

    expect(httpClient.get).toHaveBeenCalledWith('/assistant/status')
    expect(result).toEqual(status)
  })

  it('fetches the current user conversation', async () => {
    const conversation = {
      conversationId: 'conv-1',
      messages: [
        {
          id: 'msg-1',
          role: 'USER' as const,
          content: 'Hello',
          createdAt: '2026-07-01T10:00:00Z',
        },
      ],
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: conversation })

    const result = await assistantApi.getConversation()

    expect(httpClient.get).toHaveBeenCalledWith('/assistant/conversation')
    expect(result).toEqual(conversation)
  })

  it('sends a chat message', async () => {
    const request = {
      message: 'Show my profile',
      conversationId: 'conv-1',
    }
    const response = {
      response: 'Here is your profile.',
      conversationId: 'conv-1',
      intentType: 'TOOL' as const,
      toolUsed: 'my-profile',
      sources: [],
      metadata: null,
    }
    vi.mocked(httpClient.post).mockResolvedValue({ data: response })

    const result = await assistantApi.sendMessage(request)

    expect(httpClient.post).toHaveBeenCalledWith('/assistant/chat', request)
    expect(result).toEqual(response)
  })
})
