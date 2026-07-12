import {
  Box,
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
import type { GlobalLeaderboardEntry } from '../../types/leaderboards'
import { LeaderboardAvatar } from './LeaderboardAvatar'
import { LEADERBOARD_MESSAGES } from './leaderboardMessages'
import { WrappingText } from '../common/WrappingText'

interface GlobalLeaderboardTableProps {
  entries: GlobalLeaderboardEntry[]
  loading: boolean
  currentUserId?: string | null
}

export function GlobalLeaderboardTable({ entries, loading, currentUserId }: GlobalLeaderboardTableProps) {
  if (loading) {
    return (
      <Stack spacing={1}>
        {Array.from({ length: 5 }, (_, index) => (
          <Skeleton height={48} key={index} variant="rounded" />
        ))}
      </Stack>
    )
  }

  if (entries.length === 0) {
    return (
      <Typography color="text.secondary" variant="body2">
        {LEADERBOARD_MESSAGES.emptyGlobal}
      </Typography>
    )
  }

  return (
    <>
      <Box sx={{ display: { xs: 'block', md: 'none' } }}>
        <Stack spacing={1.5}>
          {entries.map((entry) => (
            <Paper
              key={entry.employee.id}
              sx={{
                p: 2,
                outline:
                  entry.employee.id === currentUserId
                    ? (theme) => `2px solid ${theme.palette.primary.main}`
                    : undefined,
              }}
              variant="outlined"
            >
              <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                <LeaderboardAvatar fullName={entry.employee.fullName} />
                <Stack spacing={0.25} sx={{ minWidth: 0, flex: 1 }}>
                  <Typography color="text.secondary" variant="overline">
                    #{entry.rank}
                  </Typography>
                  <WrappingText variant="subtitle1">{entry.employee.fullName}</WrappingText>
                  <Typography color="text.secondary" variant="body2">
                    {LEADERBOARD_MESSAGES.approvedCertification(entry.totalApprovedCertifications)}
                  </Typography>
                </Stack>
              </Stack>
            </Paper>
          ))}
        </Stack>
      </Box>

      <TableContainer sx={{ display: { xs: 'none', md: 'block' } }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>{LEADERBOARD_MESSAGES.rank}</TableCell>
              <TableCell>Employee</TableCell>
              <TableCell align="right">{LEADERBOARD_MESSAGES.approvedCertificationLabel}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entries.map((entry) => {
              const isCurrentUser = entry.employee.id === currentUserId
              return (
                <TableRow
                  key={entry.employee.id}
                  selected={isCurrentUser}
                  sx={isCurrentUser ? { '&.Mui-selected': { backgroundColor: 'action.selected' } } : undefined}
                >
                  <TableCell>#{entry.rank}</TableCell>
                  <TableCell>
                    <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', minWidth: 0 }}>
                      <LeaderboardAvatar fullName={entry.employee.fullName} size={36} />
                      <WrappingText variant="body1">{entry.employee.fullName}</WrappingText>
                    </Stack>
                  </TableCell>
                  <TableCell align="right">{entry.totalApprovedCertifications}</TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  )
}
