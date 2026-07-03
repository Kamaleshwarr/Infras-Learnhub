import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { TechnologyDetailPage } from './TechnologyDetailPage'
import { AuthContext } from '../../auth/AuthProvider'
import type { Technology } from '../../types/learn'

vi.mock('../../api/learnApi', () => ({
  learnApi: {
    getTechnology: vi.fn(),
  },
}))

import { learnApi } from '../../api/learnApi'

const technology: Technology = {
  id: '11111111-1111-1111-1111-111111111111',
  slug: 'java',
  name: 'Java',
  shortName: 'Java',
  description: 'Enterprise-grade language for backend services.',
  category: 'BACKEND',
  difficulty: 'INTERMEDIATE',
  status: 'PUBLISHED',
  featured: false,
  featuredOverride: null,
  catalogFeatured: false,
  estimatedDuration: '6-8 weeks',
  officialWebsite: 'https://www.oracle.com/java/',
  officialDocumentation: 'https://docs.oracle.com/en/java/',
  tags: ['jvm'],
  orgNotes: null,
  catalogVersion: '1.0.0',
  catalogSource: 'platform-team',
  catalogPresent: true,
  relatedProjects: [],
  createdBy: {
    id: '22222222-2222-2222-2222-222222222222',
    employeeId: 'ADMIN001',
    fullName: 'Admin',
    email: 'admin@learninghub.local',
  },
  createdAtUtc: '2026-07-02T00:00:00Z',
  updatedAtUtc: '2026-07-02T00:00:00Z',
}

function renderDetail(path = `/learn/technologies/${technology.id}`) {
  render(
    <AuthContext.Provider
      value={{
        currentRole: 'EMPLOYEE',
        hasRole: () => true,
        isAdmin: false,
        isAuthenticated: true,
        isEmployee: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        refreshProfile: vi.fn(),
        user: {
          id: 'employee-1',
          employeeId: 'EMP001',
          fullName: 'Employee',
          email: 'employee@learninghub.local',
          mustChangePassword: false,
          roles: ['EMPLOYEE'],
        },
      }}
    >
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route element={<TechnologyDetailPage />} path="/learn/technologies/:technologyId" />
          <Route element={<div>Roadmap Page</div>} path="/learn/technologies/:technologyId/roadmap" />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('TechnologyDetailPage roadmap navigation', () => {
  it('links to the roadmap page for the current technology', async () => {
    vi.mocked(learnApi.getTechnology).mockResolvedValue(technology)

    renderDetail()

    expect(await screen.findByRole('heading', { name: 'Java' })).toBeInTheDocument()

    const roadmapLink = screen.getByRole('link', { name: 'View Roadmap' })
    expect(roadmapLink).toHaveAttribute('href', `/learn/technologies/${technology.id}/roadmap`)
  })
})
