import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { KnowledgeBasePage } from './KnowledgeBasePage'
import { projectsApi } from '../../api/projectsApi'
import { projectKnowledgeApi } from '../../api/projectKnowledgeApi'

vi.mock('../../api/projectsApi', () => ({
  projectsApi: { get: vi.fn() },
}))

vi.mock('../../api/projectKnowledgeApi', () => ({
  projectKnowledgeApi: {
    getFolder: vi.fn(),
    listFolders: vi.fn(),
    listItems: vi.fn(),
  },
}))

vi.mock('../../components/project-knowledge/knowledgeUtils', () => ({
  buildFolderBreadcrumbs: vi.fn(async () => [
    { label: 'Payments', href: '/projects/project-1' },
    { label: 'Knowledge Base', href: '/projects/project-1/knowledge' },
  ]),
  canCreateSubfolder: vi.fn(() => true),
  loadAllFoldersForSelect: vi.fn(async () => []),
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: () => ({ isAdmin: false, user: { id: 'viewer-1' } }),
}))

describe('KnowledgeBasePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(projectsApi.get).mockResolvedValue({
      id: 'project-1',
      name: 'Payments Platform',
      accessType: 'PUBLIC',
      status: 'ACTIVE',
      archived: false,
      createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
      currentMemberRole: 'VIEWER',
    })
    vi.mocked(projectKnowledgeApi.listFolders).mockResolvedValue({
      content: [
        {
          id: 'folder-1',
          projectId: 'project-1',
          name: 'Requirements',
          description: 'Business and functional requirements',
          parentId: null,
          createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
          createdAtUtc: '2026-07-01T00:00:00Z',
          updatedAtUtc: '2026-07-01T00:00:00Z',
          childFolderCount: 1,
          itemCount: 2,
        },
      ],
      page: 0,
      size: 100,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      sort: [],
    })
    vi.mocked(projectKnowledgeApi.listItems).mockResolvedValue({
      content: [
        {
          id: 'item-1',
          projectId: 'project-1',
          folderId: 'folder-1',
          folderName: 'Requirements',
          title: 'Business Requirements',
          description: 'Confluence page',
          category: 'REQUIREMENTS',
          sourceType: 'LINK',
          externalUrl: 'https://example.com/requirements',
          accessCount: 0,
          uploadedBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
          createdAtUtc: '2026-07-01T00:00:00Z',
          updatedAtUtc: '2026-07-01T00:00:00Z',
        },
      ],
      page: 0,
      size: 100,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      sort: [],
    })
  })

  it('renders knowledge base home with folders and resources', async () => {
    render(
      <MemoryRouter initialEntries={['/projects/project-1/knowledge']}>
        <Routes>
          <Route element={<KnowledgeBasePage />} path="/projects/:projectId/knowledge" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Knowledge Base' })).toBeInTheDocument()
    expect(screen.getAllByText('Requirements').length).toBeGreaterThan(0)
    expect(screen.getByText('Business Requirements')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Add folder/i })).not.toBeInTheDocument()
  })
})
