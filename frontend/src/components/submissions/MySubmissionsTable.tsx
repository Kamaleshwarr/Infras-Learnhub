import {
  Alert,
  Box,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import type { CertificateSubmission } from '../../types/submissions'
import { SubmissionStatusChip } from './SubmissionStatusChip'

interface MySubmissionsTableProps {
  loading: boolean
  submissions: CertificateSubmission[]
  emptyMessage: string
}

function formatDate(value: string | null | undefined) {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export function MySubmissionsTable({ loading, submissions, emptyMessage }: MySubmissionsTableProps) {
  if (loading) {
    return (
      <Box aria-label="Loading submissions" sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (submissions.length === 0) {
    return (
      <Box sx={{ px: 3, py: 6 }}>
        <Alert severity="info">{emptyMessage}</Alert>
      </Box>
    )
  }

  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Initiative</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Submitted</TableCell>
            <TableCell>Reviewed</TableCell>
            <TableCell>Certificate file</TableCell>
            <TableCell>Rejection reason</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {submissions.map((submission) => (
            <TableRow key={submission.id} hover>
              <TableCell>
                <Typography variant="body2">{submission.initiative.title}</Typography>
              </TableCell>
              <TableCell>
                <SubmissionStatusChip status={submission.approvalStatus} />
              </TableCell>
              <TableCell>{formatDate(submission.submittedAtUtc)}</TableCell>
              <TableCell>{formatDate(submission.reviewedAtUtc)}</TableCell>
              <TableCell>{submission.certificateDocument.originalFilename}</TableCell>
              <TableCell>
                {submission.approvalStatus === 'REJECTED' && submission.rejectionReason
                  ? submission.rejectionReason
                  : '—'}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
