import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { LearnHomePage } from './LearnHomePage'
import type { Technology } from '../../types/learn'

vi.mock('../../api/learnApi', () => ({
  learnApi: {
    listTechnologies: vi.fn(),
  },
}))

import { learnApi } from '../../api/learnApi'

const featuredTechnology: Technology = {
  id: '11111111-1111-1111-1111-111111111111',
  slug: 'spring-boot',
  name: 'Spring Boot',
  shortName: 'Spring Boot',
  description: 'Opinionated framework for building production-ready JVM applications.',
  category: 'BACKEND',
  difficulty: 'INTERMEDIATE',
  status: 'PUBLISHED',
  featured: true,
  featuredOverride: true,
  catalogFeatured: false,
  catalogPresent: true,
}

function renderHome() {
  return render(
    <MemoryRouter>
      <LearnHomePage />
    </MemoryRouter>,
  )
}

describe('LearnHomePage featured section', () => {
  it('renders featured technologies in a responsive grid with actions', async () => {
    vi.mocked(learnApi.listTechnologies).mockResolvedValue({
      content: [featuredTechnology],
      page: 0,
      size: 12,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      sort: [{ property: 'name', direction: 'ASC' }],
    })

    renderHome()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Spring Boot' })).toBeInTheDocument()
    })

    expect(screen.getByRole('link', { name: 'View Roadmap' })).toHaveAttribute(
      'href',
      `/learn/technologies/${featuredTechnology.id}/roadmap`,
    )
    expect(screen.getByRole('link', { name: 'View Details' })).toHaveAttribute(
      'href',
      `/learn/technologies/${featuredTechnology.id}`,
    )
  })

  it('shows empty state when no featured technologies are published', async () => {
    vi.mocked(learnApi.listTechnologies).mockResolvedValue({
      content: [{ ...featuredTechnology, featured: false }],
      page: 0,
      size: 12,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      sort: [{ property: 'name', direction: 'ASC' }],
    })

    renderHome()

    expect(await screen.findByText('No featured technologies are published yet.')).toBeInTheDocument()
  })
})
