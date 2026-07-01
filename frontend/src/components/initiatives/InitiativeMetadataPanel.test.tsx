import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { Initiative } from '../../types/initiatives'
import { InitiativeMetadataPanel } from './InitiativeMetadataPanel'

const initiative: Initiative = {
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
  startDateUtc: '2025-01-01T00:00:00Z',
  status: 'ACTIVE',
  title: 'AWS Certification',
  updatedAtUtc: '2026-06-01T12:00:00Z',
}

describe('InitiativeMetadataPanel', () => {
  it('renders read-only metadata fields', () => {
    render(<InitiativeMetadataPanel initiative={initiative} />)

    expect(screen.getByText(/Created by: Admin User \(admin@example.com\)/i)).toBeInTheDocument()
    expect(screen.getByText(/Created on:/i)).toBeInTheDocument()
    expect(screen.getByText(/Last updated:/i)).toBeInTheDocument()
  })
})
