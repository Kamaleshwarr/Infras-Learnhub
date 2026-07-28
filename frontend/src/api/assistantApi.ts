import { httpClient } from './httpClient'
import type { AssistantStatus, ChatRequest, ChatResponse, Conversation } from '../types/assistant'

export const assistantApi = {
  getStatus: async () => {
    const response = await httpClient.get<AssistantStatus>('/assistant/status')
    return response.data
  },
  getConversation: async () => {
    const response = await httpClient.get<Conversation>('/assistant/conversation')
    return response.data
  },
  sendMessage: async (request: ChatRequest) => {
    const response = await httpClient.post<ChatResponse>('/assistant/chat', request)
    return response.data
  },
}
