import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { InitiativeListToolbar } from './InitiativeListToolbar'

describe('InitiativeListToolbar', () => {
  it('renders create initiative action', () => {
    render(<InitiativeListToolbar onCreateInitiative={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Create Initiative' })).toBeInTheDocument()
  })

  it('invokes create handler when clicked', async () => {
    const user = userEvent.setup()
    const onCreateInitiative = vi.fn()
    render(<InitiativeListToolbar onCreateInitiative={onCreateInitiative} />)

    await user.click(screen.getByRole('button', { name: 'Create Initiative' }))
    expect(onCreateInitiative).toHaveBeenCalledTimes(1)
  })
})
