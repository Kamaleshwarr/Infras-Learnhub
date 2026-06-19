import ChevronRightOutlinedIcon from '@mui/icons-material/ChevronRightOutlined'
import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  Paper,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import type { Initiative } from '../../types/initiatives'
import { SortableTableHead } from '../common/SortableTableHead'
import type { SortableColumn } from '../common/SortableTableHead'
import { formatInitiativeDate, truncateText } from './initiativeDisplay'
import { InitiativeExpiryBadge } from './InitiativeExpiryBadge'
import { InitiativeStatusChip } from './InitiativeStatusChip'

interface InitiativeTableProps {
  initiatives: Initiative[]
  loading: boolean
  sort: string
  showStatusColumn: boolean
  onSort: (property: string) => void
}

const BASE_COLUMNS: SortableColumn[] = [
  { id: 'title', label: 'Title', sortable: true },
  { id: 'expiryDateUtc', label: 'Expires', sortable: true },
  { id: 'reward', label: 'Reward' },
]

const STATUS_COLUMN: SortableColumn = { id: 'status', label: 'Status', sortable: true }

function buildColumns(showStatusColumn: boolean) {
  if (!showStatusColumn) {
    return BASE_COLUMNS
  }

  return [BASE_COLUMNS[0], STATUS_COLUMN, BASE_COLUMNS[1], BASE_COLUMNS[2]]
}

export function InitiativeTable({
  initiatives,
  loading,
  sort,
  showStatusColumn,
  onSort,
}: InitiativeTableProps) {
  const navigate = useNavigate()
  const columns = buildColumns(showStatusColumn)

  if (loading) {
    return (
      <Paper variant="outlined">
        <Box sx={{ p: 2 }}>
          {Array.from({ length: 5 }).map((_, index) => (
            <Skeleton height={48} key={index} sx={{ mb: 1 }} />
          ))}
        </Box>
      </Paper>
    )
  }

  return (
    <TableContainer component={Paper} variant="outlined">
      <Table>
        <SortableTableHead columns={columns} onSort={onSort} sort={sort} />
        <TableBody>
          {initiatives.map((initiative) => (
            <TableRow
              hover
              key={initiative.id}
              onClick={() => navigate(`/initiatives/${initiative.id}`)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                  event.preventDefault()
                  navigate(`/initiatives/${initiative.id}`)
                }
              }}
              role="button"
              sx={{ cursor: 'pointer' }}
              tabIndex={0}
            >
              <TableCell>
                <Stack spacing={0.5}>
                  <Typography variant="body2">{initiative.title}</Typography>
                  <InitiativeExpiryBadge expiryDateUtc={initiative.expiryDateUtc} />
                </Stack>
              </TableCell>
              {showStatusColumn ? (
                <TableCell>
                  <InitiativeStatusChip status={initiative.status} />
                </TableCell>
              ) : null}
              <TableCell>{formatInitiativeDate(initiative.expiryDateUtc)}</TableCell>
              <TableCell>
                {initiative.rewardDescription
                  ? truncateText(initiative.rewardDescription, 60)
                  : '—'}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

interface InitiativeCardListProps {
  initiatives: Initiative[]
  loading: boolean
  showStatusColumn: boolean
}

export function InitiativeCardList({ initiatives, loading, showStatusColumn }: InitiativeCardListProps) {
  const navigate = useNavigate()

  if (loading) {
    return (
      <Stack spacing={2}>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton height={120} key={index} variant="rounded" />
        ))}
      </Stack>
    )
  }

  return (
    <Stack spacing={2}>
      {initiatives.map((initiative) => (
        <Card key={initiative.id} variant="outlined">
          <CardActionArea onClick={() => navigate(`/initiatives/${initiative.id}`)}>
            <CardContent>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start', justifyContent: 'space-between' }}>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="subtitle1">{initiative.title}</Typography>
                  <Typography color="text.secondary" sx={{ mt: 0.5 }} variant="body2">
                    Expires {formatInitiativeDate(initiative.expiryDateUtc)}
                  </Typography>
                  {initiative.rewardDescription ? (
                    <Typography color="text.secondary" sx={{ mt: 1 }} variant="body2">
                      {truncateText(initiative.rewardDescription, 80)}
                    </Typography>
                  ) : null}
                  <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1, mt: 1 }}>
                    {showStatusColumn ? <InitiativeStatusChip status={initiative.status} /> : null}
                    <InitiativeExpiryBadge expiryDateUtc={initiative.expiryDateUtc} />
                  </Stack>
                </Box>
                <ChevronRightOutlinedIcon color="action" />
              </Stack>
            </CardContent>
          </CardActionArea>
        </Card>
      ))}
    </Stack>
  )
}
