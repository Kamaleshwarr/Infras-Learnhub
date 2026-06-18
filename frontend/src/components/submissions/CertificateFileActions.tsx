import { useState } from 'react'
import { Alert, Button, CircularProgress, Stack } from '@mui/material'
import { submissionsApi } from '../../api/submissionsApi'
import type { CertificateSubmission } from '../../types/submissions'
import { resolveApiError } from '../../utils/apiErrors'
import { downloadBlob } from '../../utils/downloadBlob'
import { CertificatePreviewDialog } from './CertificatePreviewDialog'
import { SUBMISSION_MESSAGES } from './submissionMessages'

interface CertificateFileActionsProps {
  disabled?: boolean
  submission: CertificateSubmission
}

export function CertificateFileActions({ disabled = false, submission }: CertificateFileActionsProps) {
  const [previewOpen, setPreviewOpen] = useState(false)
  const [previewBlob, setPreviewBlob] = useState<Blob | null>(null)
  const [previewLoading, setPreviewLoading] = useState(false)
  const [previewError, setPreviewError] = useState<string | null>(null)
  const [downloadLoading, setDownloadLoading] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  async function handlePreview() {
    setPreviewOpen(true)
    setPreviewLoading(true)
    setPreviewError(null)
    setPreviewBlob(null)
    setActionError(null)

    try {
      const { blob } = await submissionsApi.getCertificateBlob(submission.id, { disposition: 'inline' })
      setPreviewBlob(blob)
    } catch (error) {
      const message = resolveApiError(error, SUBMISSION_MESSAGES.certificatePreviewError)
      setPreviewError(
        message.toLowerCase().includes('not found')
          ? SUBMISSION_MESSAGES.certificateFileNotFound
          : message,
      )
    } finally {
      setPreviewLoading(false)
    }
  }

  async function handleDownload() {
    setDownloadLoading(true)
    setActionError(null)

    try {
      const { blob } = await submissionsApi.getCertificateBlob(submission.id, { disposition: 'attachment' })
      downloadBlob(blob, submission.certificateDocument.originalFilename)
    } catch (error) {
      const message = resolveApiError(error, SUBMISSION_MESSAGES.certificateDownloadError)
      setActionError(
        message.toLowerCase().includes('not found')
          ? SUBMISSION_MESSAGES.certificateFileNotFound
          : message,
      )
    } finally {
      setDownloadLoading(false)
    }
  }

  function handleClosePreview() {
    if (previewLoading) {
      return
    }
    setPreviewOpen(false)
    setPreviewBlob(null)
    setPreviewError(null)
  }

  const actionsDisabled = disabled || previewLoading || downloadLoading

  return (
    <>
      <Stack spacing={1}>
        <Stack direction="row" spacing={1}>
          <Button
            disabled={actionsDisabled}
            onClick={() => void handlePreview()}
            size="small"
            variant="outlined"
          >
            {previewLoading ? <CircularProgress aria-label="Loading certificate preview" size={20} /> : 'Preview'}
          </Button>
          <Button
            disabled={actionsDisabled}
            onClick={() => void handleDownload()}
            size="small"
            variant="outlined"
          >
            {downloadLoading ? <CircularProgress aria-label="Downloading certificate" size={20} /> : 'Download'}
          </Button>
        </Stack>
        {actionError ? <Alert severity="error">{actionError}</Alert> : null}
      </Stack>
      <CertificatePreviewDialog
        blob={previewBlob}
        error={previewError}
        loading={previewLoading}
        onClose={handleClosePreview}
        onDownload={() => void handleDownload()}
        open={previewOpen}
        submission={submission}
      />
    </>
  )
}
