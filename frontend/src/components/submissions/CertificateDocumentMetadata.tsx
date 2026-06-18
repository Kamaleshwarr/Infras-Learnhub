import { Stack, Typography } from '@mui/material'
import type { CertificateDocument } from '../../types/submissions'
import { formatCertificateContentType, formatFileSizeBytes } from './certificateDocumentDisplay'

interface CertificateDocumentMetadataProps {
  document: CertificateDocument
}

export function CertificateDocumentMetadata({ document }: CertificateDocumentMetadataProps) {
  return (
    <Stack spacing={0.25}>
      <Typography variant="body2">{document.originalFilename}</Typography>
      <Typography color="text.secondary" variant="caption">
        {formatCertificateContentType(document.contentType)} · {formatFileSizeBytes(document.fileSizeBytes)}
      </Typography>
    </Stack>
  )
}
