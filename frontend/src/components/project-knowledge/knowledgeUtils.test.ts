import { describe, expect, it } from 'vitest'
import {
  canCreateSubfolderAtDepth,
  getFolderDepthFromBreadcrumbs,
} from './knowledgeUtils'

describe('knowledgeUtils depth helpers', () => {
  it('allows subfolder at Knowledge Base home and levels 1 and 2', () => {
    expect(canCreateSubfolderAtDepth(0)).toBe(true)
    expect(canCreateSubfolderAtDepth(1)).toBe(true)
    expect(canCreateSubfolderAtDepth(2)).toBe(true)
  })

  it('blocks subfolder at level 3', () => {
    expect(canCreateSubfolderAtDepth(3)).toBe(false)
  })

  it('derives folder depth from breadcrumbs', () => {
    const home = [
      { label: 'Project', href: '/projects/p1' },
      { label: 'Knowledge Base', href: '/projects/p1/knowledge' },
    ]
    const level3 = [
      ...home,
      { label: 'QA', href: '/projects/p1/knowledge/folders/f1' },
      { label: 'Automation', href: '/projects/p1/knowledge/folders/f2' },
      { label: 'API Testing', href: '/projects/p1/knowledge/folders/f3' },
    ]

    expect(getFolderDepthFromBreadcrumbs(home)).toBe(0)
    expect(getFolderDepthFromBreadcrumbs(level3)).toBe(3)
  })
})
