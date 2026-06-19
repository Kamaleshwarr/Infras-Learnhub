import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { TopLearnerCard } from './TopLearnerCard'

const entry = {
  approvedAtUtc: '2026-06-05T00:00:00Z',
  employee: {
    email: 'jane@example.com',
    fullName: 'Jane Smith',
    id: 'employee-2',
  },
  initiativeId: 'initiative-1',
  initiativeTitle: 'AWS Certification',
  rank: 1,
  submissionId: 'submission-2',
  submittedAtUtc: '2026-06-01T00:00:00Z',
}

describe('TopLearnerCard', () => {
  it('renders top learner entry', () => {
    render(<TopLearnerCard entry={entry} error={null} loading={false} />)
    expect(screen.getByText(/#1 Jane Smith/i)).toBeInTheDocument()
  })

  it('renders empty state', () => {
    render(<TopLearnerCard entry={null} error={null} loading={false} />)
    expect(screen.getByText(/No completions yet/i)).toBeInTheDocument()
  })

  it('renders unavailable message on error', () => {
    render(<TopLearnerCard entry={null} error="failed" loading={false} />)
    expect(screen.getByText(/Top learner unavailable/i)).toBeInTheDocument()
  })
})
