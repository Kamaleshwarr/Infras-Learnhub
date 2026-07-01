import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { InitiativeDescriptionCard } from './InitiativeDescriptionCard'

describe('InitiativeDescriptionCard', () => {
  it('wraps long unbroken description text', () => {
    const longDescription = 'x'.repeat(200)
    const { container } = render(<InitiativeDescriptionCard description={longDescription} />)

    expect(screen.getByText(longDescription)).toBeInTheDocument()
    expect(container.querySelector('.MuiTypography-body1')).toHaveStyle({
      overflowWrap: 'anywhere',
      wordBreak: 'break-word',
    })
  })
})
