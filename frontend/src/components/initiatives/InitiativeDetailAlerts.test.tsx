import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { Initiative } from '../../types/initiatives'
import { InitiativeDetailAlerts } from './InitiativeDetailAlerts'

const baseInitiative: Initiative = {
  createdAtUtc: '2025-01-01T10:00:00Z',
  createdBy: {
    email: 'admin@example.com',
    employeeId: 'ADMIN001',
    fullName: 'Admin User',
    id: 'admin-1',
  },
  description: 'Program details',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-1',
  rewardDescription: '$500 credit',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE',
  title: 'AWS Certification',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

describe('InitiativeDetailAlerts', () => {
  it('renders nothing for draft initiatives', () => {
    const { container } = render(
      <InitiativeDetailAlerts initiative={{ ...baseInitiative, status: 'DRAFT' }} />,
    )

    expect(container).toBeEmptyDOMElement()
  })

  it('renders expired label for expired initiatives', () => {
    const inTenDays = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString()

    render(
      <InitiativeDetailAlerts
        initiative={{ ...baseInitiative, expiryDateUtc: inTenDays, status: 'EXPIRED' }}
      />,
    )

    expect(screen.getByText('Expired')).toBeInTheDocument()
    expect(screen.queryByText(/Expires in/i)).not.toBeInTheDocument()
  })

  it('renders countdown for active initiatives expiring soon', () => {
    const inTenDays = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString()

    render(
      <InitiativeDetailAlerts
        initiative={{ ...baseInitiative, expiryDateUtc: inTenDays, status: 'ACTIVE' }}
      />,
    )

    expect(screen.getByText(/Expires in 10 days/i)).toBeInTheDocument()
  })
})
