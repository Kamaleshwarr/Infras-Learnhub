import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProjectTeamPage } from './ProjectTeamPage'
import { projectsApi } from '../../api/projectsApi'
import { projectTeamApi } from '../../api/projectTeamApi'
import { useAuth } from '../../auth/useAuth'

vi.mock('../../api/projectsApi', () => ({
  projectsApi: {
    get: vi.fn(),
    listMembers: vi.fn(),
    addOrUpdateMember: vi.fn(),
    removeMember: vi.fn(),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: vi.fn(() => ({ isAdmin: true })),
}))

vi.mock('../../api/projectTeamApi', () => ({
  projectTeamApi: {
    listExternalContacts: vi.fn(),
    searchMemberCandidates: vi.fn(),
    createExternalContact: vi.fn(),
    updateExternalContact: vi.fn(),
    deleteExternalContact: vi.fn(),
  },
}))

const members = [
  {
    id: 'member-1',
    projectId: 'project-1',
    projectRole: 'OWNER' as const,
    functionalRole: 'PRODUCT_OWNER' as const,
    responsibility: 'Roadmap and prioritization',
    primaryContact: true,
    displayOrder: 0,
    createdAtUtc: '2026-07-01T00:00:00Z',
    updatedAtUtc: '2026-07-01T00:00:00Z',
    user: { id: 'u1', employeeId: 'E1', fullName: 'Priya S', email: 'priya@example.com' },
  },
  {
    id: 'member-2',
    projectId: 'project-1',
    projectRole: 'CONTRIBUTOR' as const,
    functionalRole: 'TECH_LEAD' as const,
    responsibility: 'Architecture and code reviews',
    primaryContact: true,
    displayOrder: 1,
    createdAtUtc: '2026-07-01T00:00:00Z',
    updatedAtUtc: '2026-07-01T00:00:00Z',
    user: { id: 'u2', employeeId: 'E2', fullName: 'Arun K', email: 'arun@example.com' },
  },
  {
    id: 'member-3',
    projectId: 'project-1',
    projectRole: 'VIEWER' as const,
    functionalRole: 'BUSINESS_ANALYST' as const,
    responsibility: null,
    primaryContact: false,
    displayOrder: 2,
    createdAtUtc: '2026-07-01T00:00:00Z',
    updatedAtUtc: '2026-07-01T00:00:00Z',
    user: { id: 'u3', employeeId: 'E3', fullName: 'Maya R', email: 'maya@example.com' },
  },
]

describe('ProjectTeamPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(projectsApi.get).mockResolvedValue({
      id: 'project-1',
      name: 'Payments',
      accessType: 'PUBLIC',
      status: 'ACTIVE',
      archived: false,
      memberCount: 3,
      primaryContactCount: 2,
      createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Priya S', email: 'priya@example.com' },
      currentMemberRole: 'OWNER',
    })
    vi.mocked(projectsApi.listMembers).mockResolvedValue(members)
    vi.mocked(projectTeamApi.listExternalContacts).mockResolvedValue([])
  })

  it('renders team summary, primary contacts, and grouped members', async () => {
    render(
      <MemoryRouter initialEntries={['/projects/project-1/team']}>
        <Routes>
          <Route element={<ProjectTeamPage />} path="/projects/:projectId/team" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Team & Contacts' })).toBeInTheDocument()
    expect(screen.getByText('3 team members')).toBeInTheDocument()
    expect(screen.getByText('2 primary contacts')).toBeInTheDocument()
    expect(screen.getAllByText('Product Owner').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Architecture and code reviews').length).toBeGreaterThan(0)
    expect(screen.getByText('No responsibility summary provided.')).toBeInTheDocument()
    expect(screen.getAllByText('Owner').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Viewer').length).toBeGreaterThan(0)
  })

  it('shows management actions for owners', async () => {
    render(
      <MemoryRouter initialEntries={['/projects/project-1/team']}>
        <Routes>
          <Route element={<ProjectTeamPage />} path="/projects/:projectId/team" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByRole('button', { name: /Add member/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Add external contact/i })).toBeInTheDocument()
  })

  it('opens add member dialog', async () => {
    const user = userEvent.setup()
    render(
      <MemoryRouter initialEntries={['/projects/project-1/team']}>
        <Routes>
          <Route element={<ProjectTeamPage />} path="/projects/:projectId/team" />
        </Routes>
      </MemoryRouter>,
    )

    await user.click(await screen.findByRole('button', { name: /Add member/i }))
    const dialog = screen.getByRole('dialog')
    expect(dialog).toBeInTheDocument()
    expect(dialog).toHaveTextContent('Add member')
  })
})

describe('ProjectTeamPage read-only', () => {
  beforeEach(() => {
    vi.mocked(projectsApi.get).mockResolvedValue({
      id: 'project-1',
      name: 'Payments',
      accessType: 'PUBLIC',
      status: 'ACTIVE',
      archived: false,
      memberCount: 3,
      primaryContactCount: 2,
      createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Priya S', email: 'priya@example.com' },
      currentMemberRole: 'VIEWER',
    })
    vi.mocked(projectsApi.listMembers).mockResolvedValue(members)
    vi.mocked(projectTeamApi.listExternalContacts).mockResolvedValue([])
  })

  it('hides management actions for viewers', async () => {
    vi.mocked(useAuth).mockReturnValue({ isAdmin: false } as ReturnType<typeof useAuth>)
    render(
      <MemoryRouter initialEntries={['/projects/project-1/team']}>
        <Routes>
          <Route element={<ProjectTeamPage />} path="/projects/:projectId/team" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Team & Contacts' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Add member/i })).not.toBeInTheDocument()
  })
})
