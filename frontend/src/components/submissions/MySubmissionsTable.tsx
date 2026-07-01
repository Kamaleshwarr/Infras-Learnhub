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
import { TruncatedTextWithTooltip } from '../common/TruncatedTextWithTooltip'
import { fixedTableSx, TEXT_DISPLAY_LIMITS } from '../common/textDisplay'
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
    <TableContainer sx={{ maxWidth: '100%' }}>
      <Table sx={fixedTableSx}>
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
              <TableCell sx={{ maxWidth: 0, width: '22%' }}>
                <TruncatedTextWithTooltip
                  maxLength={TEXT_DISPLAY_LIMITS.tableInitiative}
                  text={submission.initiative.title}
                />
              </TableCell>
              <TableCell sx={{ width: '12%' }}>
                <SubmissionStatusChip status={submission.approvalStatus} />
              </TableCell>
              <TableCell sx={{ width: '16%' }}>{formatDate(submission.submittedAtUtc)}</TableCell>
              <TableCell sx={{ width: '16%' }}>{formatDate(submission.reviewedAtUtc)}</TableCell>
              <TableCell sx={{ maxWidth: 0, width: '18%' }}>
                <TruncatedTextWithTooltip
                  maxLength={TEXT_DISPLAY_LIMITS.tableFilename}
                  text={submission.certificateDocument.originalFilename}
                />
              </TableCell>
              <TableCell sx={{ maxWidth: 0, width: '16%' }}>
                {submission.approvalStatus === 'REJECTED' && submission.rejectionReason ? (
                  <TruncatedTextWithTooltip
                    maxLength={TEXT_DISPLAY_LIMITS.tableComments}
                    text={submission.rejectionReason}
                  />
                ) : (
                  <Typography variant="body2">—</Typography>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
