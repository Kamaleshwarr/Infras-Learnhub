import { describe, expect, it } from 'vitest'
import {
  buildNavigationButtonLabel,
  extractNavigationFromChatResponse,
  parseNavigationMetadata,
} from './assistantNavigation'

describe('assistantNavigation', () => {
  it('builds the navigation button label', () => {
    expect(buildNavigationButtonLabel('Projects')).toBe('Open Projects')
    expect(buildNavigationButtonLabel('Learn')).toBe('Open Learn')
  })

  it('parses navigation metadata', () => {
    expect(
      parseNavigationMetadata({
        navigation: { path: '/projects', label: 'Projects' },
      }),
    ).toEqual({ path: '/projects', label: 'Projects' })
  })

  it('returns null for missing or invalid navigation metadata', () => {
    expect(parseNavigationMetadata(null)).toBeNull()
    expect(parseNavigationMetadata({})).toBeNull()
    expect(parseNavigationMetadata({ navigation: { path: '/projects' } })).toBeNull()
    expect(parseNavigationMetadata({ navigation: { path: '', label: 'Projects' } })).toBeNull()
  })

  it('extracts navigation from chat responses', () => {
    expect(
      extractNavigationFromChatResponse({
        response: 'Navigate to Projects.',
        conversationId: 'conv-1',
        intentType: 'NAVIGATION',
        toolUsed: null,
        sources: [],
        metadata: { navigation: { path: '/projects', label: 'Projects' } },
      }),
    ).toEqual({ path: '/projects', label: 'Projects' })
  })

  it('returns null when intent type is not navigation', () => {
    expect(
      extractNavigationFromChatResponse({
        response: 'Profile ready',
        conversationId: 'conv-1',
        intentType: 'TOOL',
        toolUsed: 'my-profile',
        sources: [],
        metadata: { navigation: { path: '/profile', label: 'Profile' } },
      }),
    ).toBeNull()
  })
})
