import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined'
import { Button, Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import type { InitiativeLeaderboardEntry } from '../../api/leaderboardsApi'
import { LEADERBOARD_MESSAGES } from '../leaderboards/leaderboardMessages'
import { WrappingText } from '../common/WrappingText'
import { formatInitiativeDate } from './initiativeDisplay'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface TopLearnerCardProps {
  initiativeId: string
  loading: boolean
  error: string | null
  entry: InitiativeLeaderboardEntry | null
}

export function TopLearnerCard({ initiativeId, loading, error, entry }: TopLearnerCardProps) {
  return (
    <Card sx={{ mb: 2, minWidth: 0 }} variant="outlined">
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
          <Stack spacing={0.5} sx={{ minWidth: 0 }}>
            <WrappingText variant="subtitle1">#{entry.rank} {entry.employee.fullName}</WrappingText>
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
        <Button
          component={RouterLink}
          size="small"
          sx={{ alignSelf: 'flex-start', mt: 1 }}
          to={`/leaderboards/initiatives/${initiativeId}`}
          variant="outlined"
        >
          {LEADERBOARD_MESSAGES.viewLeaderboard}
        </Button>
      </CardContent>
    </Card>
  )
}
