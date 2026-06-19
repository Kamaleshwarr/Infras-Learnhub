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
    id: '550E8400-E29B-41D4-A716-446655440001',
    startDateUtc: '2026-01-01T00:00:00Z',
    status: 'ACTIVE',
    title: 'AWS Certification',
  },
  {
    description: 'Azure certification program',
    expiryDateUtc: '2026-12-31T00:00:00Z',
    id: '550e8400-e29b-41d4-a716-446655440002',
    startDateUtc: '2026-01-01T00:00:00Z',
    status: 'ACTIVE',
    title: 'Azure Certification',
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
        infoMessage={null}
        initiatives={initiatives}
        loadError={null}
        loadingInitiatives={false}
        onSubmit={onSubmit}
        submittedInitiativeIds={new Set(['550e8400-e29b-41d4-a716-446655440001'])}
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
    await user.click(screen.getByRole('option', { name: 'Azure Certification' }))

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
    await user.click(screen.getByRole('option', { name: 'Azure Certification' }))

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    const certificateFile = new File(['pdf'], 'certificate.pdf', { type: 'application/pdf' })
    fireEvent.change(fileInput, { target: { files: [certificateFile] } })
    await user.type(screen.getByRole('textbox', { name: 'Comments' }), 'Passed the exam')
    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith({
        initiativeId: '550e8400-e29b-41d4-a716-446655440002',
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
    await user.click(screen.getByRole('option', { name: 'Azure Certification' }))

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(fileInput, {
      target: { files: [new File(['pdf'], 'certificate.pdf', { type: 'application/pdf' })] },
    })
    await user.click(screen.getByRole('button', { name: 'Submit certificate' }))

    await waitFor(() =>
      expect(screen.getByText(SUBMISSION_MESSAGES.duplicateSubmission)).toBeInTheDocument(),
    )
  })

  it('shows already-submitted initiatives as disabled options', async () => {
    const user = userEvent.setup()
    renderForm()

    await user.click(screen.getByRole('combobox', { name: /Initiative/i }))

    expect(screen.getByRole('option', { name: 'AWS Certification (already submitted)' })).toHaveAttribute(
      'aria-disabled',
      'true',
    )
    expect(screen.getByRole('option', { name: 'Azure Certification' })).not.toHaveAttribute('aria-disabled', 'true')
  })

  it('lists available initiatives before already-submitted initiatives', async () => {
    const user = userEvent.setup()
    renderForm({
      initiatives: [
        {
          description: 'Submitted later expiry',
          expiryDateUtc: '2026-12-31T00:00:00Z',
          id: 'initiative-aws',
          startDateUtc: '2026-01-01T00:00:00Z',
          status: 'ACTIVE',
          title: 'AWS Solutions Architect',
        },
        {
          description: 'Available',
          expiryDateUtc: '2026-09-30T00:00:00Z',
          id: 'initiative-test',
          startDateUtc: '2026-01-01T00:00:00Z',
          status: 'ACTIVE',
          title: 'Test Engineering',
        },
        {
          description: 'Submitted sooner expiry',
          expiryDateUtc: '2026-06-30T00:00:00Z',
          id: 'initiative-java',
          startDateUtc: '2026-01-01T00:00:00Z',
          status: 'ACTIVE',
          title: 'Java Spring Boot Certification - Updated',
        },
      ],
      submittedInitiativeIds: new Set(['initiative-aws', 'initiative-java']),
    })

    await user.click(screen.getByRole('combobox', { name: /Initiative/i }))

    const options = screen.getAllByRole('option').map((option) => option.textContent)
    expect(options).toEqual([
      'Test Engineering',
      'Java Spring Boot Certification - Updated (already submitted)',
      'AWS Solutions Architect (already submitted)',
    ])
  })

  it('pre-selects initiative from initialInitiativeId when available', async () => {
    renderForm({
      initialInitiativeId: '550e8400-e29b-41d4-a716-446655440002',
      submittedInitiativeIds: new Set(),
    })

    await waitFor(() =>
      expect(screen.getByRole('combobox', { name: /Initiative/i })).toHaveTextContent('Azure Certification'),
    )
  })

  it('pre-selects initiative using case-insensitive initialInitiativeId matching', async () => {
    renderForm({
      initialInitiativeId: '550E8400-E29B-41D4-A716-446655440002',
      submittedInitiativeIds: new Set(),
    })

    await waitFor(() =>
      expect(screen.getByRole('combobox', { name: /Initiative/i })).toHaveTextContent('Azure Certification'),
    )
  })

  it('does not pre-select initiative when initialInitiativeId is already submitted', async () => {
    renderForm({
      initialInitiativeId: '550E8400-E29B-41D4-A716-446655440001',
    })

    await waitFor(() => expect(screen.getByRole('combobox', { name: /Initiative/i })).toBeInTheDocument())
    expect(screen.getByRole('combobox', { name: /Initiative/i })).not.toHaveTextContent('AWS Certification')
  })

  it('does not pre-select initiative when initialInitiativeId is unknown', async () => {
    renderForm({
      initialInitiativeId: 'unknown-initiative-id',
      submittedInitiativeIds: new Set(),
    })

    await waitFor(() => expect(screen.getByRole('combobox', { name: /Initiative/i })).toBeInTheDocument())
    expect(screen.getByRole('combobox', { name: /Initiative/i })).not.toHaveTextContent('AWS Certification')
    expect(screen.getByRole('combobox', { name: /Initiative/i })).not.toHaveTextContent('Azure Certification')
  })

  it('shows empty-state messaging when no initiatives are available', () => {
    renderForm({
      emptyMessage: SUBMISSION_MESSAGES.noInitiativesAvailable,
      initiatives: [],
      submittedInitiativeIds: new Set(),
    })

    expect(screen.getByText(SUBMISSION_MESSAGES.noInitiativesAvailable)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Submit certificate' })).toBeDisabled()
  })
})
