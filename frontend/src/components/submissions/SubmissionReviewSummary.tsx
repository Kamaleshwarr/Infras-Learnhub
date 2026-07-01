import { Box, Stack } from '@mui/material'
import { WrappingText } from '../common/WrappingText'
import type { CertificateSubmission } from '../../types/submissions'
import { CertificateDocumentMetadata } from './CertificateDocumentMetadata'
import { CertificateFileActions } from './CertificateFileActions'

interface SubmissionReviewSummaryProps {
  submission: CertificateSubmission
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export function SubmissionReviewSummary({ submission }: SubmissionReviewSummaryProps) {
  return (
    <Box
      sx={{
        bgcolor: 'action.hover',
        borderRadius: 1,
        minWidth: 0,
        p: 2,
      }}
    >
      <WrappingText variant="subtitle2">{submission.employee.fullName}</WrappingText>
      <WrappingText color="text.secondary" variant="body2">
        {submission.employee.employeeId} · {submission.employee.email}
      </WrappingText>
      <WrappingText sx={{ mt: 1 }} variant="body2">
        Initiative: {submission.initiative.title}
      </WrappingText>
      <WrappingText color="text.secondary" variant="body2">
        Submitted: {formatDate(submission.submittedAtUtc)}
      </WrappingText>
      {submission.comments ? (
        <WrappingText color="text.secondary" sx={{ mt: 1 }} variant="body2">
          Comments: {submission.comments}
        </WrappingText>
      ) : null}
      <Stack spacing={1} sx={{ minWidth: 0, mt: 1 }}>
        <WrappingText variant="body2">Certificate file</WrappingText>
        <CertificateDocumentMetadata document={submission.certificateDocument} />
        <CertificateFileActions submission={submission} />
      </Stack>
    </Box>
  )
}
