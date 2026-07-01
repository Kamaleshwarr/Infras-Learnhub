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
import { TruncatedTextWithTooltip } from '../common/TruncatedTextWithTooltip'
import { fixedTableSx, TEXT_DISPLAY_LIMITS } from '../common/textDisplay'
import type { CertificateSubmission } from '../../types/submissions'
import { CertificateDocumentMetadata } from './CertificateDocumentMetadata'
import { CertificateFileActions } from './CertificateFileActions'

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
    <TableContainer sx={{ maxWidth: '100%' }}>
      <Table sx={fixedTableSx}>
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
              <TableCell sx={{ maxWidth: 0, width: '18%' }}>
                <Stack spacing={0.25} sx={{ minWidth: 0 }}>
                  <TruncatedTextWithTooltip
                    maxLength={TEXT_DISPLAY_LIMITS.tableName}
                    text={submission.employee.fullName}
                  />
                  <TruncatedTextWithTooltip
                    color="text.secondary"
                    maxLength={TEXT_DISPLAY_LIMITS.tableEmployeeId}
                    text={submission.employee.employeeId}
                    variant="caption"
                  />
                </Stack>
              </TableCell>
              <TableCell sx={{ maxWidth: 0, width: '18%' }}>
                <TruncatedTextWithTooltip
                  maxLength={TEXT_DISPLAY_LIMITS.tableInitiative}
                  text={submission.initiative.title}
                />
              </TableCell>
              <TableCell sx={{ width: '14%' }}>{formatDate(submission.submittedAtUtc)}</TableCell>
              <TableCell sx={{ maxWidth: 0, width: '18%' }}>
                {submission.comments ? (
                  <TruncatedTextWithTooltip
                    maxLength={TEXT_DISPLAY_LIMITS.tableComments}
                    text={submission.comments}
                  />
                ) : (
                  <Typography variant="body2">—</Typography>
                )}
              </TableCell>
              <TableCell sx={{ maxWidth: 0, width: '18%' }}>
                <Stack spacing={1} sx={{ minWidth: 0 }}>
                  <CertificateDocumentMetadata document={submission.certificateDocument} truncate />
                  <CertificateFileActions disabled={actionDisabled} submission={submission} />
                </Stack>
              </TableCell>
              <TableCell align="right" sx={{ width: '14%' }}>
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
