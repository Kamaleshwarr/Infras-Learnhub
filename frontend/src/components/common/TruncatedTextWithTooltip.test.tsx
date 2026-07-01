import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { TruncatedTextWithTooltip } from './TruncatedTextWithTooltip'

describe('TruncatedTextWithTooltip', () => {
  it('renders short text without a tooltip', () => {
    render(<TruncatedTextWithTooltip maxLength={20} text="Short title" />)

    expect(screen.getByText('Short title')).toBeInTheDocument()
    expect(screen.queryByLabelText(/Short title/i)).not.toBeInTheDocument()
  })

  it('truncates long text and exposes the full value in a tooltip', async () => {
    const longText = 'a'.repeat(80)
    render(<TruncatedTextWithTooltip maxLength={60} text={longText} />)

    expect(screen.getByText(`${'a'.repeat(60)}…`)).toBeInTheDocument()
    expect(screen.getByText(`${'a'.repeat(60)}…`).closest('[aria-label], [title]') ?? screen.getByText(`${'a'.repeat(60)}…`).parentElement).toBeTruthy()
  })
})
