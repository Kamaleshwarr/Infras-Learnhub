import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { FeaturedTechnologyCard } from './FeaturedTechnologyCard'
import type { Technology } from '../../types/learn'

const technology: Technology = {
  id: '11111111-1111-1111-1111-111111111111',
  slug: 'java',
  name: 'Java',
  shortName: 'Java',
  description: 'Enterprise-grade language for backend services and large-scale systems.',
  category: 'BACKEND',
  difficulty: 'INTERMEDIATE',
  status: 'PUBLISHED',
  featured: true,
  featuredOverride: null,
  catalogFeatured: true,
  catalogPresent: true,
}

function renderCard(overrides: Partial<Technology> = {}) {
  return render(
    <MemoryRouter>
      <FeaturedTechnologyCard technology={{ ...technology, ...overrides }} />
    </MemoryRouter>,
  )
}

describe('FeaturedTechnologyCard', () => {
  it('renders technology metadata and actions', () => {
    renderCard()

    expect(screen.getByRole('heading', { name: 'Java' })).toBeInTheDocument()
    expect(screen.getByText('Enterprise-grade language for backend services and large-scale systems.')).toBeInTheDocument()
    expect(screen.getByText('Backend')).toBeInTheDocument()
    expect(screen.getByText('Intermediate')).toBeInTheDocument()

    const roadmapLink = screen.getByRole('link', { name: 'View Roadmap' })
    expect(roadmapLink).toHaveAttribute('href', `/learn/technologies/${technology.id}/roadmap`)

    const detailsLink = screen.getByRole('link', { name: 'View Details' })
    expect(detailsLink).toHaveAttribute('href', `/learn/technologies/${technology.id}`)
  })

  it('falls back to short name when description is missing', () => {
    renderCard({ description: null })

    expect(screen.getAllByText('Java').length).toBeGreaterThanOrEqual(2)
  })
})
