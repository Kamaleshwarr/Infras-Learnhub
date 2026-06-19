import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { InitiativeCardList, InitiativeTable } from './InitiativeListViews'

const initiative = {
  description: 'AWS certification program',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-1',
  rewardDescription: 'Learning credit',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE' as const,
  title: 'AWS Certification',
}

describe('InitiativeListViews', () => {
  it('renders table rows with title and reward', () => {
    render(
      <MemoryRouter>
        <InitiativeTable
          initiatives={[initiative]}
          loading={false}
          onSort={vi.fn()}
          showStatusColumn={false}
          sort="expiryDateUtc,asc"
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('AWS Certification')).toBeInTheDocument()
    expect(screen.getByText('Learning credit')).toBeInTheDocument()
  })

  it('shows status column for admin view', () => {
    render(
      <MemoryRouter>
        <InitiativeTable
          initiatives={[initiative]}
          loading={false}
          onSort={vi.fn()}
          showStatusColumn
          sort="expiryDateUtc,asc"
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Active')).toBeInTheDocument()
  })

  it('renders mobile cards', () => {
    render(
      <MemoryRouter>
        <InitiativeCardList initiatives={[initiative]} loading={false} showStatusColumn={false} />
      </MemoryRouter>,
    )

    expect(screen.getByText('AWS Certification')).toBeInTheDocument()
    expect(screen.getByText(/Expires/i)).toBeInTheDocument()
  })
})
