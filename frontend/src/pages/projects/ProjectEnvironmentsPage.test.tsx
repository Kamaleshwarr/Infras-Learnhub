import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProjectEnvironmentsPage } from './ProjectEnvironmentsPage'
import { projectsApi } from '../../api/projectsApi'
import { projectEnvironmentsApi } from '../../api/projectEnvironmentsApi'

vi.mock('../../api/projectsApi', () => ({ projectsApi: { get: vi.fn() } }))
vi.mock('../../api/projectEnvironmentsApi', () => ({ projectEnvironmentsApi: { list: vi.fn() } }))
vi.mock('../../auth/useAuth', () => ({ useAuth: () => ({ isAdmin: true }) }))

describe('ProjectEnvironmentsPage', () => {
  beforeEach(() => {
    vi.mocked(projectsApi.get).mockResolvedValue({
      id: 'project-1',
      name: 'Payments',
      accessType: 'PUBLIC',
      status: 'ACTIVE',
      archived: false,
      createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
      currentMemberRole: 'OWNER',
    })
    vi.mocked(projectEnvironmentsApi.list).mockResolvedValue([
      {
        id: 'env-1',
        projectId: 'project-1',
        name: 'QA',
        description: 'Quality',
        displayOrder: 0,
        active: true,
        createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
        createdAtUtc: '2026-07-01T00:00:00Z',
        updatedAtUtc: '2026-07-01T00:00:00Z',
        referenceCount: 1,
        references: [
          {
            id: 'ref-1',
            environmentId: 'env-1',
            name: 'Swagger',
            referenceType: 'SWAGGER',
            url: 'https://example.com/swagger',
            displayOrder: 0,
            active: true,
            createdAtUtc: '2026-07-01T00:00:00Z',
            updatedAtUtc: '2026-07-01T00:00:00Z',
          },
        ],
      },
    ])
  })

  it('renders environments with references', async () => {
    render(
      <MemoryRouter initialEntries={['/projects/project-1/environments']}>
        <Routes>
          <Route element={<ProjectEnvironmentsPage />} path="/projects/:projectId/environments" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Environments' })).toBeInTheDocument()
    expect(screen.getByText('QA')).toBeInTheDocument()
    expect(screen.getByText('Swagger')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Add Environment/i })).toBeInTheDocument()
  })
})
