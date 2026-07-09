import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProjectDetailPage } from './ProjectDetailPage'
import { projectsApi } from '../../api/projectsApi'

vi.mock('../../api/projectsApi', () => ({
  projectsApi: {
    get: vi.fn(),
    listMembers: vi.fn(),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: () => ({
    isAdmin: false,
    user: { id: 'emp-1', fullName: 'Employee User' },
  }),
}))

const sampleProject = {
  id: 'project-1',
  name: 'Payments Platform',
  description: 'Core payments modernization',
  accessType: 'PUBLIC' as const,
  status: 'ACTIVE' as const,
  archived: false,
  createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin User', email: 'admin@example.com' },
  owner: { id: 'u2', employeeId: 'E2', fullName: 'Owner User', email: 'owner@example.com' },
  memberCount: 2,
  currentMemberRole: 'VIEWER' as const,
  relatedTechnologies: [{ id: 'tech-1', name: 'Spring Boot', shortName: 'Spring' }],
  createdAtUtc: '2026-07-01T00:00:00Z',
  updatedAtUtc: '2026-07-02T00:00:00Z',
}

describe('ProjectDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(projectsApi.get).mockResolvedValue(sampleProject)
    vi.mocked(projectsApi.listMembers).mockResolvedValue([
      {
        id: 'member-1',
        projectId: 'project-1',
        projectRole: 'OWNER',
        createdAtUtc: '2026-07-01T00:00:00Z',
        updatedAtUtc: '2026-07-01T00:00:00Z',
        user: sampleProject.owner!,
      },
    ])
  })

  it('renders overview sections and technology stack', async () => {
    render(
      <MemoryRouter initialEntries={['/projects/project-1']}>
        <Routes>
          <Route element={<ProjectDetailPage />} path="/projects/:projectId" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByText('Payments Platform')).toBeInTheDocument()
    expect(screen.getByText('Spring Boot')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Knowledge Base/i })).toBeInTheDocument()
    expect(screen.getAllByText(/coming in a future release/i).length).toBe(3)
    expect(screen.queryByRole('button', { name: /edit project/i })).not.toBeInTheDocument()
  })
})
