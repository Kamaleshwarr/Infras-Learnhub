import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProjectRepositoriesPage } from './ProjectRepositoriesPage'
import { projectsApi } from '../../api/projectsApi'
import { projectRepositoriesApi } from '../../api/projectRepositoriesApi'

vi.mock('../../api/projectsApi', () => ({ projectsApi: { get: vi.fn() } }))
vi.mock('../../api/projectRepositoriesApi', () => ({ projectRepositoriesApi: { list: vi.fn() } }))
vi.mock('../../auth/useAuth', () => ({ useAuth: () => ({ isAdmin: false, user: { id: 'v1' } }) }))

describe('ProjectRepositoriesPage', () => {
  beforeEach(() => {
    vi.mocked(projectsApi.get).mockResolvedValue({
      id: 'project-1',
      name: 'Payments',
      accessType: 'PUBLIC',
      status: 'ACTIVE',
      archived: false,
      createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
      currentMemberRole: 'VIEWER',
    })
    vi.mocked(projectRepositoriesApi.list).mockResolvedValue([
      {
        id: 'repo-1',
        projectId: 'project-1',
        name: 'Backend Service',
        description: 'API',
        repositoryType: 'BACKEND',
        provider: 'GITHUB',
        repositoryUrl: 'https://github.com/example/backend',
        defaultBranch: 'main',
        displayOrder: 0,
        active: true,
        createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
        createdAtUtc: '2026-07-01T00:00:00Z',
        updatedAtUtc: '2026-07-01T00:00:00Z',
      },
    ])
  })

  it('renders repositories read-only for viewer', async () => {
    render(
      <MemoryRouter initialEntries={['/projects/project-1/repositories']}>
        <Routes>
          <Route element={<ProjectRepositoriesPage />} path="/projects/:projectId/repositories" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Repositories' })).toBeInTheDocument()
    expect(screen.getByText('Backend Service')).toBeInTheDocument()
    expect(screen.getByText('Default branch: main')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Add Repository/i })).not.toBeInTheDocument()
  })
})
