import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { DashboardListCard } from './DashboardListCard'

describe('DashboardListCard', () => {
  it('truncates long list item text with ellipsis', () => {
    render(
      <DashboardListCard
        emptyText="No items"
        items={[
          {
            id: '1',
            primary: 'p'.repeat(80),
            secondary: 's'.repeat(90),
          },
        ]}
        title="Recent items"
      />,
    )

    expect(screen.getByText(`${'p'.repeat(60)}…`)).toBeInTheDocument()
    expect(screen.getByText(`${'s'.repeat(80)}…`)).toBeInTheDocument()
  })
})
