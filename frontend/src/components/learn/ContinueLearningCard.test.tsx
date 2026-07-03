import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { ContinueLearningCard } from './ContinueLearningCard'
import type { ContinueLearning } from '../../types/progress'

const continueLearning: ContinueLearning = {
  enrollmentId: '44444444-4444-4444-4444-444444444444',
  technologyId: '11111111-1111-1111-1111-111111111111',
  technologySlug: 'java',
  technologyName: 'Java',
  currentStageId: '22222222-2222-2222-2222-222222222222',
  currentStageOrder: 2,
  currentStageTitle: 'Core Java',
  progressPercent: 25,
}

describe('ContinueLearningCard', () => {
  it('renders active journey summary and continue link', () => {
    render(
      <MemoryRouter>
        <ContinueLearningCard continueLearning={continueLearning} />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: 'Continue Learning' })).toBeInTheDocument()
    expect(screen.getByText('Java')).toBeInTheDocument()
    expect(screen.getByText(/Current stage: Core Java/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Continue' })).toHaveAttribute(
      'href',
      `/learn/technologies/${continueLearning.technologyId}/roadmap#stage-2`,
    )
  })
})
