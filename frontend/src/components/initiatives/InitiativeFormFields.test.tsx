import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { InitiativeFormFields } from './InitiativeFormFields'
import { createEmptyInitiativeForm } from './initiativeFormState'

describe('InitiativeFormFields', () => {
  it('renders shared initiative form fields', () => {
    const onChange = vi.fn()
    render(
      <InitiativeFormFields
        onChange={onChange}
        values={createEmptyInitiativeForm(Date.parse('2026-06-19T12:00:00.000Z'))}
      />,
    )

    expect(screen.getByLabelText(/^Title/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^Description/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^Reward \/ Benefits/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^Start date \(UTC\)/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^Expiry date \(UTC\)/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/^Status/i)).not.toBeInTheDocument()
  })

  it('calls onChange when a field is edited', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    render(
      <InitiativeFormFields
        onChange={onChange}
        values={createEmptyInitiativeForm(Date.parse('2026-06-19T12:00:00.000Z'))}
      />,
    )

    await user.type(screen.getByLabelText(/^Title/i), 'A')
    expect(onChange).toHaveBeenCalled()
  })

  it('shows field errors', () => {
    render(
      <InitiativeFormFields
        fieldErrors={{ title: 'Title is required.' }}
        onChange={vi.fn()}
        values={createEmptyInitiativeForm(Date.parse('2026-06-19T12:00:00.000Z'))}
      />,
    )

    expect(screen.getByText('Title is required.')).toBeInTheDocument()
  })
})
