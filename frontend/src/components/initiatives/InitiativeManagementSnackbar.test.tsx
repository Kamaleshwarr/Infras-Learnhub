import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { InitiativeManagementSnackbar } from './InitiativeManagementSnackbar'

describe('InitiativeManagementSnackbar', () => {
  it('shows success notification', () => {
    render(
      <InitiativeManagementSnackbar
        notification={{ message: 'Initiative created.', severity: 'success' }}
        onClose={vi.fn()}
      />,
    )

    expect(screen.getByText('Initiative created.')).toBeInTheDocument()
  })

  it('ignores clickaway dismiss', () => {
    const onClose = vi.fn()
    render(
      <InitiativeManagementSnackbar
        notification={{ message: 'Initiative updated.', severity: 'success' }}
        onClose={onClose}
      />,
    )

    fireEvent.click(document.body)
    expect(onClose).not.toHaveBeenCalled()
  })
})
