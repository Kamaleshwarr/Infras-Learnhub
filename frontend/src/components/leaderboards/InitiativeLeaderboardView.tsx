import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Paper,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import type { Initiative } from '../../types/initiatives'
import type { InitiativeLeaderboardEntry } from '../../types/leaderboards'
import { formatInitiativeDate } from '../initiatives/initiativeDisplay'
import { WrappingText } from '../common/WrappingText'
import { LeaderboardAvatar } from './LeaderboardAvatar'
import { LEADERBOARD_MESSAGES } from './leaderboardMessages'

interface InitiativeLeaderboardViewProps {
  initiativeTitle: string
  initiative?: Initiative | null
  entries: InitiativeLeaderboardEntry[]
  totalElements: number
  loading: boolean
  error: string | null
  currentUserId?: string | null
  onRetry?: () => void
}

export function InitiativeLeaderboardView({
  initiativeTitle,
  initiative,
  entries,
  totalElements,
  loading,
  error,
  currentUserId,
  onRetry,
}: InitiativeLeaderboardViewProps) {
  const currentUserEntry = entries.find((entry) => entry.employee.id === currentUserId)

  if (error) {
    return (
      <Alert
        action={
          onRetry ? (
            <Button color="inherit" onClick={onRetry} size="small">
              {LEADERBOARD_MESSAGES.retry}
            </Button>
          ) : undefined
        }
        severity="error"
      >
        {error}
      </Alert>
    )
  }

  return (
    <Stack spacing={3}>
      <Card variant="outlined">
        <CardContent>
          {loading ? (
            <Stack spacing={1.5}>
              <Skeleton height={32} width="45%" />
              <Skeleton height={20} width="80%" />
              <Skeleton height={48} width="60%" />
            </Stack>
          ) : (
            <Stack spacing={2}>
              <Stack spacing={0.5}>
                <Typography variant="h5">{initiativeTitle}</Typography>
                {initiative?.description ? (
                  <Typography color="text.secondary" variant="body2">
                    {initiative.description}
                  </Typography>
                ) : null}
              </Stack>

              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={{ xs: 2, sm: 4 }}
                sx={{ alignItems: { sm: 'center' } }}
              >
                <Stack spacing={0.5}>
                  <Typography variant="overline">{LEADERBOARD_MESSAGES.initiativeRankedEmployees}</Typography>
                  <Typography variant="h5">{totalElements}</Typography>
                </Stack>
                {currentUserId && (currentUserEntry || totalElements === 0) ? (
                  <Stack spacing={0.5}>
                    <Typography variant="overline">{LEADERBOARD_MESSAGES.initiativeYourRank}</Typography>
                    <Typography variant="h5">
                      {currentUserEntry ? `#${currentUserEntry.rank}` : LEADERBOARD_MESSAGES.myRankUnranked}
                    </Typography>
                  </Stack>
                ) : null}
              </Stack>

              <Typography color="text.secondary" variant="body2">
                {LEADERBOARD_MESSAGES.initiativeRankingRule}
              </Typography>
            </Stack>
          )}
        </CardContent>
      </Card>

      {loading ? (
        <Stack spacing={1}>
          {Array.from({ length: 5 }, (_, index) => (
            <Skeleton height={48} key={index} variant="rounded" />
          ))}
        </Stack>
      ) : entries.length === 0 ? (
        <Paper sx={{ p: 3 }} variant="outlined">
          <Typography color="text.secondary" variant="body1">
            {LEADERBOARD_MESSAGES.emptyInitiative}
          </Typography>
        </Paper>
      ) : (
        <>
          <Box sx={{ display: { xs: 'block', md: 'none' } }}>
            <Stack spacing={1.5}>
              {entries.map((entry) => (
                <Paper
                  key={entry.submissionId}
                  sx={{
                    p: 2,
                    outline:
                      entry.employee.id === currentUserId
                        ? (theme) => `2px solid ${theme.palette.primary.main}`
                        : undefined,
                  }}
                  variant="outlined"
                >
                  <Stack spacing={0.5}>
                    <Typography color="text.secondary" variant="overline">
                      #{entry.rank} {entry.employee.fullName}
                    </Typography>
                    <Typography color="text.secondary" variant="body2">
                      {LEADERBOARD_MESSAGES.submitted}: {formatInitiativeDate(entry.submittedAtUtc)}
                    </Typography>
                    <Typography color="text.secondary" variant="body2">
                      {LEADERBOARD_MESSAGES.approved}: {formatInitiativeDate(entry.approvedAtUtc)}
                    </Typography>
                  </Stack>
                </Paper>
              ))}
            </Stack>
          </Box>

          <TableContainer component={Paper} variant="outlined" sx={{ display: { xs: 'none', md: 'block' } }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>{LEADERBOARD_MESSAGES.rank}</TableCell>
                  <TableCell>Employee</TableCell>
                  <TableCell>{LEADERBOARD_MESSAGES.submitted}</TableCell>
                  <TableCell>{LEADERBOARD_MESSAGES.approved}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {entries.map((entry) => {
                  const isCurrentUser = entry.employee.id === currentUserId
                  return (
                    <TableRow key={entry.submissionId} selected={isCurrentUser}>
                      <TableCell>#{entry.rank}</TableCell>
                      <TableCell>
                        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', minWidth: 0 }}>
                          <LeaderboardAvatar fullName={entry.employee.fullName} size={36} />
                          <WrappingText variant="body1">{entry.employee.fullName}</WrappingText>
                        </Stack>
                      </TableCell>
                      <TableCell>{formatInitiativeDate(entry.submittedAtUtc)}</TableCell>
                      <TableCell>{formatInitiativeDate(entry.approvedAtUtc)}</TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}
    </Stack>
  )
}
