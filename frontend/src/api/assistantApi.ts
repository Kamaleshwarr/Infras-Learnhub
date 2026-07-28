import { httpClient } from './httpClient'
import type {
  AssistantChatRequest,
  AssistantChatResponse,
  AssistantStatus,
  ConversationResponse,
} from '../types/assistant'

export const assistantApi = {
  getStatus: async () => {
    const response = await httpClient.get<AssistantStatus>('/assistant/status')
    return response.data
  },
  getConversation: async () => {
    const response = await httpClient.get<ConversationResponse>('/assistant/conversation')
    return response.data
  },
  sendMessage: async (request: AssistantChatRequest) => {
    const response = await httpClient.post<AssistantChatResponse>('/assistant/chat', request)
    return response.data
  },
}
