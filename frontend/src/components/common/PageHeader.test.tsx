import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { PageHeader } from './PageHeader'

describe('PageHeader', () => {
  it('wraps long unbroken titles and descriptions', () => {
    const longTitle = 'T'.repeat(120)
    const longDescription = 'D'.repeat(120)
    const { container } = render(<PageHeader description={longDescription} title={longTitle} />)

    expect(screen.getByRole('heading', { level: 1, name: longTitle })).toBeInTheDocument()
    expect(screen.getByText(longDescription)).toBeInTheDocument()
    expect(container.querySelector('h1')).toHaveStyle({
      overflowWrap: 'anywhere',
      wordBreak: 'break-word',
    })
  })
})
