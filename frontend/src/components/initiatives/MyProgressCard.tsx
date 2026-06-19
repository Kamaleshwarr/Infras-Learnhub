import Chip from '@mui/material/Chip'
import { Alert, Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import type { CertificateSubmission } from '../../types/submissions'
import { SubmissionStatusChip } from '../submissions/SubmissionStatusChip'
import { formatInitiativeDate } from './initiativeDisplay'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface MyProgressCardProps {
  loading: boolean
  error: string | null
  submission: CertificateSubmission | null
}

export function MyProgressCard({ loading, error, submission }: MyProgressCardProps) {
  return (
    <Card sx={{ mb: 2 }} variant="outlined">
      <CardContent>
        <Typography gutterBottom variant="h6">
          {INITIATIVE_MESSAGES.myProgress}
        </Typography>

        {loading ? (
          <Stack spacing={1}>
            <Skeleton height={28} width="50%" />
            <Skeleton height={20} width="70%" />
          </Stack>
        ) : error ? (
          <Alert severity="warning">{error}</Alert>
        ) : submission ? (
          <Stack spacing={1}>
            <SubmissionStatusChip status={submission.approvalStatus} />
            <Typography color="text.secondary" variant="body2">
              Submitted {formatInitiativeDate(submission.submittedAtUtc)}
            </Typography>
            {submission.approvalStatus === 'REJECTED' ? (
              <Typography color="text.secondary" variant="body2">
                {INITIATIVE_MESSAGES.progressRejectedHelper}
              </Typography>
            ) : null}
          </Stack>
        ) : (
          <Chip label={INITIATIVE_MESSAGES.progressNotSubmitted} size="small" variant="outlined" />
        )}
      </CardContent>
    </Card>
  )
}
