import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { KnowledgeFolderCard } from './KnowledgeFolderCard'
import type { ProjectKnowledgeFolder } from '../../types/projectKnowledge'

const baseFolder: ProjectKnowledgeFolder = {
  id: 'folder-1',
  projectId: 'project-1',
  name: 'Deployment',
  description: 'Deployment guides and runbooks',
  parentId: null,
  createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
  createdAtUtc: '2026-07-01T00:00:00Z',
  updatedAtUtc: '2026-07-01T00:00:00Z',
  childFolderCount: 0,
  itemCount: 0,
}

describe('KnowledgeFolderCard', () => {
  it('renders description and empty-state summary', () => {
    render(
      <MemoryRouter>
        <KnowledgeFolderCard folder={baseFolder} href="/projects/project-1/knowledge/folders/folder-1" />
      </MemoryRouter>,
    )

    expect(screen.getByText('Deployment')).toBeInTheDocument()
    expect(screen.getByText('Deployment guides and runbooks')).toBeInTheDocument()
    expect(screen.getByText('No resources yet')).toBeInTheDocument()
  })

  it('omits description when absent and shows counts', () => {
    render(
      <MemoryRouter>
        <KnowledgeFolderCard
          folder={{
            ...baseFolder,
            name: 'Quality Assurance',
            description: undefined,
            childFolderCount: 3,
            itemCount: 0,
          }}
          href="/projects/project-1/knowledge/folders/folder-2"
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Quality Assurance')).toBeInTheDocument()
    expect(screen.queryByText(/undefined|null/i)).not.toBeInTheDocument()
    expect(screen.getByText('3 sub-areas')).toBeInTheDocument()
  })

  it('fills the width of its parent container', () => {
    const { container } = render(
      <MemoryRouter>
        <KnowledgeFolderCard folder={baseFolder} href="/projects/project-1/knowledge/folders/folder-1" />
      </MemoryRouter>,
    )
    const card = container.querySelector('.MuiCard-root')
    expect(card).toHaveStyle({ width: '100%' })
  })

  it('wraps long folder names', () => {
    render(
      <MemoryRouter>
        <KnowledgeFolderCard
          folder={{
            ...baseFolder,
            name: 'Enterprise Platform Deployment and Release Engineering',
            description: undefined,
          }}
          href="/projects/project-1/knowledge/folders/folder-3"
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Enterprise Platform Deployment and Release Engineering')).toBeInTheDocument()
  })
})
