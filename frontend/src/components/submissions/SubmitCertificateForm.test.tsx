import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { InitiativeSummary } from '../../api/initiativesApi'
import { SubmitCertificateForm } from './SubmitCertificateForm'
import { SUBMISSION_MESSAGES } from './submissionMessages'

const initiatives: InitiativeSummary[] = [
  {
    description: 'AWS certification program',
    expiryDateUtc: '2026-12-31T00:00:00Z',
    id: 'initiative-1',
    startDateUtc: '2026-01-01T00:00:00Z',
    status: 'ACTIVE',
    title: 'AWS Certification',
  },
]

describe('SubmitCertificateForm', () => {
  const onSubmit = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  function renderForm(overrides: Partial<Parameters<typeof SubmitCertificateForm>[0]> = {}) {
    return render(
      <SubmitCertificateForm
        emptyMessage={null}
        initiatives={initiatives}
        loadError={null}
        loadingInitiatives={false}
        onSubmit={onSubmit}
        submitting={false}
        {...overrides}
      />,
    )
  }

  it('shows validation errors when required fields are missing', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    expect(screen.getByText(SUBMISSION_MESSAGES.initiativeRequired)).toBeInTheDocument()
    expect(screen.getByText(SUBMISSION_MESSAGES.fileRequired)).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('rejects unsupported certificate files before submit', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.click(screen.getByRole('combobox', { name: /Initiative/i }))
    await user.click(screen.getByRole('option', { name: 'AWS Certification' }))

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    const invalidFile = new File(['doc'], 'certificate.doc', { type: 'application/msword' })
    fireEvent.change(fileInput, { target: { files: [invalidFile] } })
    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    expect(screen.getByText(SUBMISSION_MESSAGES.fileValidationError)).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits selected initiative, file, and comments', async () => {
    const user = userEvent.setup()
    onSubmit.mockResolvedValue(undefined)
    renderForm()

    await user.click(screen.getByRole('combobox', { name: /Initiative/i }))
    await user.click(screen.getByRole('option', { name: 'AWS Certification' }))

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    const certificateFile = new File(['pdf'], 'certificate.pdf', { type: 'application/pdf' })
    fireEvent.change(fileInput, { target: { files: [certificateFile] } })
    await user.type(screen.getByRole('textbox', { name: 'Comments' }), 'Passed the exam')
    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith({
        initiativeId: 'initiative-1',
        file: certificateFile,
        comments: 'Passed the exam',
      }),
    )
  })

  it('shows duplicate submission errors from the API', async () => {
    const user = userEvent.setup()
    const error = new axios.AxiosError(
      'Request failed',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        data: {
          message: 'A certificate submission already exists for this initiative',
        },
        headers: {},
        status: 400,
        statusText: 'Bad Request',
        config: { headers: new axios.AxiosHeaders() },
      },
    )
    onSubmit.mockRejectedValue(error)
    renderForm()

    await user.click(screen.getByRole('combobox', { name: /Initiative/i }))
    await user.click(screen.getByRole('option', { name: 'AWS Certification' }))

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(fileInput, {
      target: { files: [new File(['pdf'], 'certificate.pdf', { type: 'application/pdf' })] },
    })
    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    await waitFor(() =>
      expect(screen.getByText(SUBMISSION_MESSAGES.duplicateSubmission)).toBeInTheDocument(),
    )
  })

  it('shows empty-state messaging when no initiatives are available', () => {
    renderForm({
      emptyMessage: SUBMISSION_MESSAGES.noInitiativesAvailable,
      initiatives: [],
    })

    expect(screen.getByText(SUBMISSION_MESSAGES.noInitiativesAvailable)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Submit certificate' })).toBeDisabled()
  })
})
