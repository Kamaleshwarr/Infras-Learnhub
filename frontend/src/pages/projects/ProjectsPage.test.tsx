import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProjectsPage } from './ProjectsPage'
import { projectsApi } from '../../api/projectsApi'

vi.mock('../../api/projectsApi', () => ({
  projectsApi: {
    list: vi.fn(),
    create: vi.fn(),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: () => ({ isAdmin: true, user: { id: 'admin-1', fullName: 'Admin User' } }),
}))

const sampleProject = {
  id: 'project-1',
  name: 'Payments Platform',
  description: 'Core payments modernization',
  accessType: 'PUBLIC' as const,
  status: 'ACTIVE' as const,
  archived: false,
  owner: { id: 'u1', employeeId: 'E1', fullName: 'Owner User', email: 'owner@example.com' },
  memberCount: 3,
  relatedTechnologies: [{ id: 'tech-1', name: 'Java', shortName: 'Java' }],
}

describe('ProjectsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(projectsApi.list).mockResolvedValue({
      content: [sampleProject],
      page: 0,
      size: 12,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      sort: [],
    })
  })

  it('renders project cards and supports search', async () => {
    const user = userEvent.setup()
    render(
      <MemoryRouter initialEntries={['/projects']}>
        <Routes>
          <Route element={<ProjectsPage />} path="/projects" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByText('Payments Platform')).toBeInTheDocument()
    expect(screen.getByText('Java')).toBeInTheDocument()

    await user.type(screen.getByPlaceholderText(/search projects/i), 'payments')

    await waitFor(() => {
      expect(projectsApi.list).toHaveBeenCalled()
    })
  })

  it('shows empty state when no projects are returned', async () => {
    vi.mocked(projectsApi.list).mockResolvedValue({
      content: [],
      page: 0,
      size: 12,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
      sort: [],
    })

    render(
      <MemoryRouter initialEntries={['/projects']}>
        <Routes>
          <Route element={<ProjectsPage />} path="/projects" />
        </Routes>
      </MemoryRouter>,
    )

    expect(await screen.findByText(/no projects found/i)).toBeInTheDocument()
  })
})
