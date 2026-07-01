import { Stack, Typography } from '@mui/material'
import { TruncatedTextWithTooltip } from '../common/TruncatedTextWithTooltip'
import { TEXT_DISPLAY_LIMITS } from '../common/textDisplay'
import { WrappingText } from '../common/WrappingText'
import type { CertificateDocument } from '../../types/submissions'
import { formatCertificateContentType, formatFileSizeBytes } from './certificateDocumentDisplay'

interface CertificateDocumentMetadataProps {
  document: CertificateDocument
  truncate?: boolean
}

export function CertificateDocumentMetadata({ document, truncate = false }: CertificateDocumentMetadataProps) {
  return (
    <Stack spacing={0.25} sx={{ minWidth: 0 }}>
      {truncate ? (
        <TruncatedTextWithTooltip
          maxLength={TEXT_DISPLAY_LIMITS.tableFilename}
          text={document.originalFilename}
        />
      ) : (
        <WrappingText variant="body2">{document.originalFilename}</WrappingText>
      )}
      <Typography color="text.secondary" variant="caption">
        {formatCertificateContentType(document.contentType)} · {formatFileSizeBytes(document.fileSizeBytes)}
      </Typography>
    </Stack>
  )
}
