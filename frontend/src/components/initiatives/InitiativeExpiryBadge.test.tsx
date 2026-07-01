import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { InitiativeExpiryBadge } from './InitiativeExpiryBadge'

describe('InitiativeExpiryBadge', () => {
  it('renders countdown when active initiative expires within 14 days', () => {
    const inTenDays = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString()

    render(<InitiativeExpiryBadge expiryDateUtc={inTenDays} status="ACTIVE" />)

    expect(screen.getByText(/Expires in 10 days/i)).toBeInTheDocument()
  })

  it('renders nothing for draft initiatives', () => {
    const inTenDays = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString()

    const { container } = render(<InitiativeExpiryBadge expiryDateUtc={inTenDays} status="DRAFT" />)

    expect(container).toBeEmptyDOMElement()
  })

  it('renders expired label for expired initiatives', () => {
    const inTenDays = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString()

    render(<InitiativeExpiryBadge expiryDateUtc={inTenDays} status="EXPIRED" />)

    expect(screen.getByText('Expired')).toBeInTheDocument()
    expect(screen.queryByText(/Expires in/i)).not.toBeInTheDocument()
  })

  it('renders nothing when active expiry is outside the window', () => {
    const inThirtyDays = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()

    const { container } = render(<InitiativeExpiryBadge expiryDateUtc={inThirtyDays} status="ACTIVE" />)

    expect(container).toBeEmptyDOMElement()
  })
})
