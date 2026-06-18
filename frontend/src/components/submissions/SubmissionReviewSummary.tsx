import { Box, Stack, Typography } from '@mui/material'
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
        p: 2,
      }}
    >
      <Typography variant="subtitle2">{submission.employee.fullName}</Typography>
      <Typography color="text.secondary" variant="body2">
        {submission.employee.employeeId} · {submission.employee.email}
      </Typography>
      <Typography sx={{ mt: 1 }} variant="body2">
        Initiative: {submission.initiative.title}
      </Typography>
      <Typography color="text.secondary" variant="body2">
        Submitted: {formatDate(submission.submittedAtUtc)}
      </Typography>
      {submission.comments ? (
        <Typography color="text.secondary" sx={{ mt: 1 }} variant="body2">
          Comments: {submission.comments}
        </Typography>
      ) : null}
      <Stack spacing={1} sx={{ mt: 1 }}>
        <Typography variant="body2">Certificate file</Typography>
        <CertificateDocumentMetadata document={submission.certificateDocument} />
        <CertificateFileActions submission={submission} />
      </Stack>
    </Box>
  )
}
