import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import type { CertificateSubmission } from '../../types/submissions'

interface AdminReviewTableProps {
  emptyMessage: string
  loading: boolean
  onApprove: (submission: CertificateSubmission) => void
  onReject: (submission: CertificateSubmission) => void
  submissions: CertificateSubmission[]
  actionDisabled?: boolean
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function truncateComments(comments: string | null | undefined) {
  if (!comments) {
    return '—'
  }

  return comments.length > 120 ? `${comments.slice(0, 117)}...` : comments
}

export function AdminReviewTable({
  actionDisabled = false,
  emptyMessage,
  loading,
  onApprove,
  onReject,
  submissions,
}: AdminReviewTableProps) {
  if (loading) {
    return (
      <Box aria-label="Loading pending submissions" sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
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
            <TableCell>Employee</TableCell>
            <TableCell>Initiative</TableCell>
            <TableCell>Submitted</TableCell>
            <TableCell>Comments</TableCell>
            <TableCell>Certificate file</TableCell>
            <TableCell align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {submissions.map((submission) => (
            <TableRow key={submission.id} hover>
              <TableCell>
                <Stack spacing={0.25}>
                  <Typography variant="body2">{submission.employee.fullName}</Typography>
                  <Typography color="text.secondary" variant="caption">
                    {submission.employee.employeeId}
                  </Typography>
                </Stack>
              </TableCell>
              <TableCell>
                <Typography variant="body2">{submission.initiative.title}</Typography>
              </TableCell>
              <TableCell>{formatDate(submission.submittedAtUtc)}</TableCell>
              <TableCell>{truncateComments(submission.comments)}</TableCell>
              <TableCell>{submission.certificateDocument.originalFilename}</TableCell>
              <TableCell align="right">
                <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
                  <Button
                    color="success"
                    disabled={actionDisabled}
                    onClick={() => onApprove(submission)}
                    size="small"
                    variant="outlined"
                  >
                    Approve
                  </Button>
                  <Button
                    color="error"
                    disabled={actionDisabled}
                    onClick={() => onReject(submission)}
                    size="small"
                    variant="outlined"
                  >
                    Reject
                  </Button>
                </Stack>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
