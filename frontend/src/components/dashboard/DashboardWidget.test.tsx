import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { DashboardWidget } from './DashboardWidget'

describe('DashboardWidget', () => {
  it('renders a navigable widget when href is provided', () => {
    render(
      <MemoryRouter>
        <DashboardWidget
          helperText="Certificate submissions awaiting review"
          href="/submissions/review"
          linkAriaLabel="View 5 pending certificate reviews"
          title="Pending Reviews"
          value="5"
        />
      </MemoryRouter>,
    )

    const link = screen.getByRole('link', { name: 'View 5 pending certificate reviews' })
    expect(link).toHaveAttribute('href', '/submissions/review')
    expect(screen.getByText('Pending Reviews')).toBeInTheDocument()
    expect(screen.getByText('5')).toBeInTheDocument()
  })

  it('renders a static widget when href is omitted', () => {
    render(
      <DashboardWidget
        helperText="Currently active learning programs"
        title="Active Initiatives"
        value="12"
      />,
    )

    expect(screen.queryByRole('link')).not.toBeInTheDocument()
    expect(screen.getByText('Active Initiatives')).toBeInTheDocument()
  })
})
