import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { WrappingText } from './WrappingText'

describe('WrappingText', () => {
  it('renders full content with wrap styles applied', () => {
    const longText = 'x'.repeat(120)
    render(<WrappingText>{longText}</WrappingText>)

    expect(screen.getByText(longText)).toBeInTheDocument()
  })
})
