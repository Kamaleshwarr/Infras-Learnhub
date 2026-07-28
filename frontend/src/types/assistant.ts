export type AssistantMessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM'

export type AssistantIntentType =
  | 'NAVIGATION'
  | 'TOOL'
  | 'KNOWLEDGE'
  | 'UNKNOWN'
  | 'DISABLED'

export type AssistantSourceConfidence = 'HIGH' | 'LOW'

export interface AssistantStatus {
  enabled: boolean
  llmProvider: string
  llmHealthy: boolean
}

export interface AssistantSource {
  serviceName: string
  toolName: string | null
  confidence: AssistantSourceConfidence
}

export interface AssistantNavigationMetadata {
  path: string
  label: string
}

export interface AssistantChatRequest {
  message: string
  conversationId?: string | null
}

export interface AssistantChatResponse {
  response: string
  conversationId: string
  intentType: AssistantIntentType
  toolUsed: string | null
  sources: AssistantSource[]
  metadata: Record<string, unknown>
}

export interface ConversationMessage {
  id: string
  role: AssistantMessageRole
  content: string
  createdAt: string
}

export interface ConversationResponse {
  conversationId: string | null
  messages: ConversationMessage[]
}

export interface AssistantUiMessage {
  id: string
  role: AssistantMessageRole
  content: string
  createdAt?: string
  intentType?: AssistantIntentType
  metadata?: Record<string, unknown>
  pending?: boolean
  error?: boolean
}
