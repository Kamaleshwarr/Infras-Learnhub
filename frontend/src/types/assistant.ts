export type AssistantMessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM'

export type AssistantIntentType = 'NAVIGATION' | 'TOOL' | 'KNOWLEDGE' | 'UNKNOWN'

export type AssistantSourceConfidence = 'HIGH' | 'LOW'

export interface AssistantStatus {
  enabled: boolean
  llmProvider: string
  llmHealthy: boolean
}

export interface AssistantNavigation {
  path: string
  label: string
}

export interface ConversationMessage {
  id: string
  role: AssistantMessageRole
  content: string
  createdAt: string | null
  navigation?: AssistantNavigation | null
}

export interface Conversation {
  conversationId: string | null
  messages: ConversationMessage[]
}

export interface AssistantSource {
  serviceName: string
  toolName: string | null
  confidence: AssistantSourceConfidence
}

export interface ChatRequest {
  message: string
  conversationId?: string | null
}

export interface ChatResponse {
  response: string
  conversationId: string
  intentType: AssistantIntentType
  toolUsed: string | null
  sources: AssistantSource[]
  metadata: Record<string, unknown> | null
}
