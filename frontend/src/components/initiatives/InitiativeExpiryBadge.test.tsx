import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { InitiativeExpiryBadge } from './InitiativeExpiryBadge'

describe('InitiativeExpiryBadge', () => {
  it('renders when initiative expires within 14 days', () => {
    const inTenDays = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString()

    render(<InitiativeExpiryBadge expiryDateUtc={inTenDays} />)

    expect(screen.getByText(/Expires in 10 days/i)).toBeInTheDocument()
  })

  it('renders nothing when expiry is outside the window', () => {
    const inThirtyDays = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()

    const { container } = render(<InitiativeExpiryBadge expiryDateUtc={inThirtyDays} />)

    expect(container).toBeEmptyDOMElement()
  })
})
