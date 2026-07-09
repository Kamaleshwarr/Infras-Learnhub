import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { KnowledgeResourceCard } from './KnowledgeResourceCard'
import type { ProjectKnowledgeItem } from '../../types/projectKnowledge'

const baseItem: ProjectKnowledgeItem = {
  id: 'item-1',
  projectId: 'project-1',
  folderId: 'folder-1',
  folderName: 'Requirements',
  title: 'Business Requirements',
  description: 'Confluence page for business requirements',
  category: 'REQUIREMENTS',
  sourceType: 'LINK',
  externalUrl: 'https://example.com/requirements',
  accessCount: 0,
  uploadedBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
  createdAtUtc: '2026-07-01T00:00:00Z',
  updatedAtUtc: '2026-07-01T00:00:00Z',
}

describe('KnowledgeResourceCard', () => {
  it('renders description and hostname when present', () => {
    render(
      <KnowledgeResourceCard canManage={false} item={baseItem} onOpen={vi.fn()} />,
    )

    expect(screen.getByText('Business Requirements')).toBeInTheDocument()
    expect(screen.getByText('Confluence page for business requirements')).toBeInTheDocument()
    expect(screen.getByText('example.com')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /open resource/i })).toBeInTheDocument()
  })

  it('omits description when absent without placeholder text', () => {
    render(
      <KnowledgeResourceCard
        canManage={false}
        item={{ ...baseItem, description: undefined }}
        onOpen={vi.fn()}
      />,
    )

    expect(screen.getByText('Business Requirements')).toBeInTheDocument()
    expect(screen.queryByText('Confluence page for business requirements')).not.toBeInTheDocument()
    expect(screen.queryByText(/undefined|null/i)).not.toBeInTheDocument()
    expect(screen.getByText('example.com')).toBeInTheDocument()
  })

  it('renders long titles and preserves action buttons', () => {
    const onEdit = vi.fn()
    render(
      <KnowledgeResourceCard
        canManage
        item={{
          ...baseItem,
          title: 'Enterprise Architecture Decision Record for Payments Platform Modernization',
          description: undefined,
        }}
        onDelete={vi.fn()}
        onEdit={onEdit}
        onOpen={vi.fn()}
      />,
    )

    expect(
      screen.getByText('Enterprise Architecture Decision Record for Payments Platform Modernization'),
    ).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /edit resource/i })).toBeInTheDocument()
  })
})
