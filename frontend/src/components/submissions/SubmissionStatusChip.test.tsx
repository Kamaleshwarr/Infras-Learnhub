import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { SubmissionStatusChip } from './SubmissionStatusChip'

describe('SubmissionStatusChip', () => {
  it.each([
    ['SUBMITTED', 'Submitted'],
    ['APPROVED', 'Approved'],
    ['REJECTED', 'Rejected'],
  ] as const)('renders %s status label', (status, label) => {
    render(<SubmissionStatusChip status={status} />)

    expect(screen.getByText(label)).toBeInTheDocument()
  })
})
