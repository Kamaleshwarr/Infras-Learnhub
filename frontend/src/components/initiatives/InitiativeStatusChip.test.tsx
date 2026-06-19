import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { InitiativeStatusChip } from './InitiativeStatusChip'

describe('InitiativeStatusChip', () => {
  it('renders active status', () => {
    render(<InitiativeStatusChip status="ACTIVE" />)
    expect(screen.getByText('Active')).toBeInTheDocument()
  })

  it('renders draft status', () => {
    render(<InitiativeStatusChip status="DRAFT" />)
    expect(screen.getByText('Draft')).toBeInTheDocument()
  })
})
