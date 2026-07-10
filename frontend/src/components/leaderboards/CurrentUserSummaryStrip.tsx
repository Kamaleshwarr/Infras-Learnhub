import { Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import type { PersonalLeaderboard } from '../../types/leaderboards'
import { LEADERBOARD_MESSAGES } from './leaderboardMessages'

interface CurrentUserSummaryStripProps {
  loading: boolean
  personal: PersonalLeaderboard | null
}

export function CurrentUserSummaryStrip({ loading, personal }: CurrentUserSummaryStripProps) {
  return (
    <Card sx={{ mb: 3 }} variant="outlined">
      <CardContent>
        {loading ? (
          <Stack spacing={1}>
            <Skeleton height={28} width="30%" />
            <Skeleton height={20} width="50%" />
          </Stack>
        ) : (
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={{ xs: 1, sm: 4 }}
            sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
          >
            <Stack spacing={0.5}>
              <Typography variant="overline">{LEADERBOARD_MESSAGES.myRank}</Typography>
              <Typography variant="h5">
                {personal?.globalRank ? `#${personal.globalRank}` : LEADERBOARD_MESSAGES.myRankUnranked}
              </Typography>
              <Typography color="text.secondary" variant="body2">
                {LEADERBOARD_MESSAGES.currentUserSummary}
              </Typography>
            </Stack>
            <Stack spacing={0.5}>
              <Typography variant="overline">{LEADERBOARD_MESSAGES.approvedCertificationLabel}</Typography>
              <Typography variant="h5">{personal?.totalApprovedCertifications ?? 0}</Typography>
            </Stack>
          </Stack>
        )}
      </CardContent>
    </Card>
  )
}
