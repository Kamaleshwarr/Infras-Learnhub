import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
} from '@mui/material'
import type { CertificateSubmission } from '../../types/submissions'
import { WrappingText } from '../common/WrappingText'
import { CertificateDocumentMetadata } from './CertificateDocumentMetadata'
import { isImageContentType, isPdfContentType } from './certificateDocumentDisplay'

interface CertificatePreviewDialogProps {
  blob: Blob | null
  error?: string | null
  loading?: boolean
  onClose: () => void
  onDownload: () => void
  open: boolean
  submission: CertificateSubmission | null
}

export function CertificatePreviewDialog({
  blob,
  error = null,
  loading = false,
  onClose,
  onDownload,
  open,
  submission,
}: CertificatePreviewDialogProps) {
  const [blobUrl, setBlobUrl] = useState<string | null>(null)
  const contentType = submission?.certificateDocument.contentType ?? blob?.type ?? ''

  useEffect(() => {
    if (!open || !blob) {
      setBlobUrl(null)
      return
    }

    const url = URL.createObjectURL(blob)
    setBlobUrl(url)

    return () => {
      URL.revokeObjectURL(url)
    }
  }, [blob, open])

  const previewContent = useMemo(() => {
    if (loading) {
      return (
        <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'center', minHeight: 320, py: 4 }}>
          <CircularProgress aria-label="Loading certificate preview" />
        </Box>
      )
    }

    if (error) {
      return <Alert severity="error">{error}</Alert>
    }

    if (!blobUrl) {
      return <Alert severity="info">Certificate preview is not available.</Alert>
    }

    if (isPdfContentType(contentType)) {
      return (
        <Box
          component="iframe"
          src={blobUrl}
          sx={{ border: 0, minHeight: 480, width: '100%' }}
          title="Certificate preview"
        />
      )
    }

    if (isImageContentType(contentType)) {
      return (
        <Box
          alt="Certificate preview"
          component="img"
          src={blobUrl}
          sx={{ display: 'block', maxHeight: 480, maxWidth: '100%', objectFit: 'contain' }}
        />
      )
    }

    return <Alert severity="warning">Preview is not supported for this file type. Download the certificate instead.</Alert>
  }, [blobUrl, contentType, error, loading])

  return (
    <Dialog
      aria-labelledby="certificate-preview-title"
      fullWidth
      maxWidth={isImageContentType(contentType) ? 'sm' : 'md'}
      onClose={loading ? undefined : onClose}
      open={open}
    >
      <DialogTitle id="certificate-preview-title">Certificate preview</DialogTitle>
      <DialogContent sx={{ minWidth: 0 }}>
        <Stack spacing={2} sx={{ minWidth: 0, pt: 1 }}>
          {submission ? (
            <Box sx={{ minWidth: 0 }}>
              <WrappingText variant="subtitle2">{submission.employee.fullName}</WrappingText>
              <WrappingText color="text.secondary" variant="body2">
                {submission.initiative.title}
              </WrappingText>
              <Box sx={{ mt: 1 }}>
                <CertificateDocumentMetadata document={submission.certificateDocument} />
              </Box>
            </Box>
          ) : null}
          {previewContent}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button disabled={loading} onClick={onClose}>
          Close
        </Button>
        <Button disabled={loading || Boolean(error)} onClick={onDownload} variant="outlined">
          Download
        </Button>
      </DialogActions>
    </Dialog>
  )
}
