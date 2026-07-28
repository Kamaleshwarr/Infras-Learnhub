import type { AssistantNavigation, ChatResponse } from '../../types/assistant'

export function buildNavigationButtonLabel(label: string) {
  return `Open ${label}`
}

export function parseNavigationMetadata(metadata: Record<string, unknown> | null | undefined) {
  if (!metadata || typeof metadata.navigation !== 'object' || metadata.navigation === null) {
    return null
  }

  const navigation = metadata.navigation as Record<string, unknown>
  if (typeof navigation.path !== 'string' || typeof navigation.label !== 'string') {
    return null
  }

  const path = navigation.path.trim()
  const label = navigation.label.trim()
  if (!path || !label) {
    return null
  }

  return { path, label } satisfies AssistantNavigation
}

export function extractNavigationFromChatResponse(response: ChatResponse) {
  if (response.intentType !== 'NAVIGATION') {
    return null
  }
  return parseNavigationMetadata(response.metadata)
}
