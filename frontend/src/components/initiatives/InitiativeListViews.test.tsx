import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
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
    expect(screen.getByText('Reward / Benefits')).toBeInTheDocument()
    expect(screen.getByText('Learning credit')).toBeInTheDocument()
  })

  it('truncates long title and reward in the table with ellipsis', () => {
    const longInitiative = {
      ...initiative,
      rewardDescription: 'r'.repeat(80),
      title: 't'.repeat(80),
    }

    render(
      <MemoryRouter>
        <InitiativeTable
          initiatives={[longInitiative]}
          loading={false}
          onSort={vi.fn()}
          showStatusColumn={false}
          sort="expiryDateUtc,asc"
        />
      </MemoryRouter>,
    )

    expect(screen.getByText(`${'t'.repeat(60)}…`)).toBeInTheDocument()
    expect(screen.getByText(`${'r'.repeat(60)}…`)).toBeInTheDocument()
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

  it('truncates long title and reward in cards with ellipsis', () => {
    const longInitiative = {
      ...initiative,
      rewardDescription: 'r'.repeat(100),
      title: 't'.repeat(100),
    }

    render(
      <MemoryRouter>
        <InitiativeCardList initiatives={[longInitiative]} loading={false} showStatusColumn={false} />
      </MemoryRouter>,
    )

    expect(screen.getByText(`${'t'.repeat(80)}…`)).toBeInTheDocument()
    expect(screen.getByText(`${'r'.repeat(80)}…`)).toBeInTheDocument()
  })

  it('shows full text in tooltip on hover for truncated table title', async () => {
    const user = userEvent.setup()
    const longTitle = 't'.repeat(80)

    render(
      <MemoryRouter>
        <InitiativeTable
          initiatives={[{ ...initiative, title: longTitle }]}
          loading={false}
          onSort={vi.fn()}
          showStatusColumn={false}
          sort="expiryDateUtc,asc"
        />
      </MemoryRouter>,
    )

    await user.hover(screen.getByText(`${'t'.repeat(60)}…`))

    expect(await screen.findByRole('tooltip')).toHaveTextContent(longTitle)
  })

  it('shows edit action for admin table view', async () => {
    const user = userEvent.setup()
    const onEdit = vi.fn()

    render(
      <MemoryRouter>
        <InitiativeTable
          initiatives={[initiative]}
          loading={false}
          onEdit={onEdit}
          onSort={vi.fn()}
          showStatusColumn
          sort="expiryDateUtc,asc"
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: /Edit initiative AWS Certification/i }))
    expect(onEdit).toHaveBeenCalledWith(initiative)
  })
})
