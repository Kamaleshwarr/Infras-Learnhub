import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { InitiativeActionBar } from './InitiativeActionBar'

describe('InitiativeActionBar', () => {
  it('links to submit certificate when no submission exists', () => {
    render(
      <MemoryRouter>
        <InitiativeActionBar initiativeId="initiative-1" loading={false} submission={null} />
      </MemoryRouter>,
    )

    expect(screen.getByRole('link', { name: /Submit Certificate/i })).toHaveAttribute(
      'href',
      '/submissions/new?initiativeId=initiative-1',
    )
  })

  it('links to my submissions when submission exists', () => {
    render(
      <MemoryRouter>
        <InitiativeActionBar
          initiativeId="initiative-1"
          loading={false}
          submission={{
            approvalStatus: 'SUBMITTED',
            certificateDocument: {
              contentType: 'application/pdf',
              fileSizeBytes: 1024,
              id: 'document-1',
              originalFilename: 'certificate.pdf',
            },
            certificateDocumentId: 'document-1',
            createdAtUtc: '2026-06-01T00:00:00Z',
            employee: {
              email: 'employee@example.com',
              employeeId: 'EMP001',
              fullName: 'Employee One',
              id: 'employee-1',
            },
            id: 'submission-1',
            initiative: {
              id: 'initiative-1',
              status: 'ACTIVE',
              title: 'AWS Certification',
            },
            submittedAtUtc: '2026-06-01T00:00:00Z',
            updatedAtUtc: '2026-06-01T00:00:00Z',
          }}
        />
      </MemoryRouter>,
    )

    expect(screen.getByRole('link', { name: /View My Submission/i })).toHaveAttribute('href', '/submissions')
    expect(screen.queryByRole('link', { name: /Submit Certificate/i })).not.toBeInTheDocument()
  })
})
