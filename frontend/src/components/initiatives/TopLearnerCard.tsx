import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined'
import { Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import type { InitiativeLeaderboardEntry } from '../../api/leaderboardsApi'
import { formatInitiativeDate } from './initiativeDisplay'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface TopLearnerCardProps {
  loading: boolean
  error: string | null
  entry: InitiativeLeaderboardEntry | null
}

export function TopLearnerCard({ loading, error, entry }: TopLearnerCardProps) {
  return (
    <Card sx={{ mb: 2 }} variant="outlined">
      <CardContent>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <EmojiEventsOutlinedIcon color="primary" fontSize="small" />
          <Typography variant="h6">{INITIATIVE_MESSAGES.topLearner}</Typography>
        </Stack>

        {loading ? (
          <Stack spacing={1}>
            <Skeleton height={28} width="70%" />
            <Skeleton height={20} width="50%" />
          </Stack>
        ) : error ? (
          <Typography color="text.secondary" variant="body2">
            {INITIATIVE_MESSAGES.topLearnerUnavailable}
          </Typography>
        ) : entry ? (
          <Stack spacing={0.5}>
            <Typography variant="subtitle1">#{entry.rank} {entry.employee.fullName}</Typography>
            <Typography color="text.secondary" variant="body2">
              {INITIATIVE_MESSAGES.topLearnerApproved(formatInitiativeDate(entry.approvedAtUtc))}
            </Typography>
            <Typography color="text.secondary" variant="caption">
              {INITIATIVE_MESSAGES.topLearnerEarliest}
            </Typography>
          </Stack>
        ) : (
          <Typography color="text.secondary" variant="body2">
            {INITIATIVE_MESSAGES.topLearnerEmpty}
          </Typography>
        )}
      </CardContent>
    </Card>
  )
}
