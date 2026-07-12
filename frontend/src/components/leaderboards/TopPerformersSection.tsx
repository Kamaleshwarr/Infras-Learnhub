import type { ReactNode } from 'react'
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined'
import { Box, Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import type { GlobalLeaderboardEntry } from '../../types/leaderboards'
import { WrappingText } from '../common/WrappingText'
import { LeaderboardAvatar } from './LeaderboardAvatar'
import { LEADERBOARD_MESSAGES } from './leaderboardMessages'

interface TopPerformersSectionProps {
  entries: GlobalLeaderboardEntry[]
  loading: boolean
  currentUserId?: string | null
}

export function TopPerformersSection({ entries, loading, currentUserId }: TopPerformersSectionProps) {
  const topEntries = entries.slice(0, 3)

  return (
    <Stack spacing={2} sx={{ mb: 3 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <EmojiEventsOutlinedIcon color="primary" fontSize="small" />
        <Typography variant="h6">{LEADERBOARD_MESSAGES.topPerformers}</Typography>
      </Stack>
      <BoxGrid>
        {loading
          ? Array.from({ length: 3 }, (_, index) => (
              <Card key={index} sx={{ minWidth: 0 }} variant="outlined">
                <CardContent>
                  <Skeleton height={28} width="60%" />
                  <Skeleton height={20} width="40%" />
                </CardContent>
              </Card>
            ))
          : topEntries.map((entry) => (
              <Card
                key={entry.employee.id}
                sx={{
                  minWidth: 0,
                  outline:
                    entry.employee.id === currentUserId
                      ? (theme) => `2px solid ${theme.palette.primary.main}`
                      : undefined,
                }}
                variant="outlined"
              >
                <CardContent>
                  <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', minWidth: 0 }}>
                    <LeaderboardAvatar fullName={entry.employee.fullName} size={44} />
                    <Stack spacing={0.25} sx={{ minWidth: 0 }}>
                      <Typography color="text.secondary" variant="overline">
                        #{entry.rank}
                      </Typography>
                      <WrappingText variant="subtitle1">{entry.employee.fullName}</WrappingText>
                      <Typography color="text.secondary" variant="body2">
                        {LEADERBOARD_MESSAGES.approvedCertification(entry.totalApprovedCertifications)}
                      </Typography>
                    </Stack>
                  </Stack>
                </CardContent>
              </Card>
            ))}
      </BoxGrid>
      {!loading && topEntries.length === 0 ? (
        <Typography color="text.secondary" variant="body2">
          {LEADERBOARD_MESSAGES.emptyGlobal}
        </Typography>
      ) : null}
    </Stack>
  )
}

function BoxGrid({ children }: { children: ReactNode }) {
  return (
    <Box
      sx={{
        display: 'grid',
        gap: 2,
        gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' },
      }}
    >
      {children}
    </Box>
  )
}
