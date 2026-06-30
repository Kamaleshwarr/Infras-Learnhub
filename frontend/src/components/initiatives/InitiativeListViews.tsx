import ChevronRightOutlinedIcon from '@mui/icons-material/ChevronRightOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  IconButton,
  Paper,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import type { Initiative, InitiativeLifecycleAction } from '../../types/initiatives'
import { TruncatedTextWithTooltip } from '../common/TruncatedTextWithTooltip'
import { SortableTableHead } from '../common/SortableTableHead'
import type { SortableColumn } from '../common/SortableTableHead'
import { fixedTableSx, TEXT_DISPLAY_LIMITS } from '../common/textDisplay'
import { formatInitiativeDate } from './initiativeDisplay'
import { InitiativeExpiryBadge } from './InitiativeExpiryBadge'
import { InitiativeLifecycleActions } from './InitiativeLifecycleActions'
import { InitiativeStatusChip } from './InitiativeStatusChip'

interface InitiativeTableProps {
  initiatives: Initiative[]
  loading: boolean
  sort: string
  showStatusColumn: boolean
  onSort: (property: string) => void
  onEdit?: (initiative: Initiative) => void
  onLifecycleSuccess?: (action: InitiativeLifecycleAction, updated: Initiative) => void
}

const BASE_COLUMNS: SortableColumn[] = [
  { id: 'title', label: 'Title', sortable: true },
  { id: 'expiryDateUtc', label: 'Expires', sortable: true },
  { id: 'reward', label: 'Reward / Benefits' },
]

const STATUS_COLUMN: SortableColumn = { id: 'status', label: 'Status', sortable: true }

const ACTIONS_COLUMN: SortableColumn = {
  id: 'actions',
  align: 'right',
  label: 'Actions',
}

function buildColumns(showStatusColumn: boolean, showActionsColumn: boolean) {
  const columns = showStatusColumn
    ? [BASE_COLUMNS[0], STATUS_COLUMN, BASE_COLUMNS[1], BASE_COLUMNS[2]]
    : [...BASE_COLUMNS]

  if (showActionsColumn) {
    columns.push(ACTIONS_COLUMN)
  }

  return columns
}

export function InitiativeTable({
  initiatives,
  loading,
  onEdit,
  onLifecycleSuccess,
  sort,
  showStatusColumn,
  onSort,
}: InitiativeTableProps) {
  const navigate = useNavigate()
  const showActionsColumn = Boolean(onEdit || onLifecycleSuccess)
  const columns = buildColumns(showStatusColumn, showActionsColumn)

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
    <TableContainer component={Paper} sx={{ maxWidth: '100%' }} variant="outlined">
      <Table sx={fixedTableSx}>
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
              <TableCell sx={{ maxWidth: 0, width: showActionsColumn ? (showStatusColumn ? '30%' : '36%') : showStatusColumn ? '36%' : '42%' }}>
                <Stack spacing={0.5} sx={{ minWidth: 0 }}>
                  <TruncatedTextWithTooltip
                    maxLength={TEXT_DISPLAY_LIMITS.tableTitle}
                    text={initiative.title}
                  />
                  <InitiativeExpiryBadge expiryDateUtc={initiative.expiryDateUtc} status={initiative.status} />
                </Stack>
              </TableCell>
              {showStatusColumn ? (
                <TableCell sx={{ width: showActionsColumn ? '10%' : '12%' }}>
                  <InitiativeStatusChip status={initiative.status} />
                </TableCell>
              ) : null}
              <TableCell sx={{ width: showActionsColumn ? (showStatusColumn ? '14%' : '18%') : showStatusColumn ? '18%' : '22%' }}>
                {formatInitiativeDate(initiative.expiryDateUtc)}
              </TableCell>
              <TableCell sx={{ maxWidth: 0, width: showActionsColumn ? (showStatusColumn ? '28%' : '32%') : showStatusColumn ? '34%' : '36%' }}>
                {initiative.rewardDescription ? (
                  <TruncatedTextWithTooltip
                    maxLength={TEXT_DISPLAY_LIMITS.tableReward}
                    text={initiative.rewardDescription}
                  />
                ) : (
                  <Typography variant="body2">—</Typography>
                )}
              </TableCell>
              {showActionsColumn ? (
                <TableCell align="right" sx={{ width: showStatusColumn ? '16%' : '18%' }} onClick={(event) => event.stopPropagation()}>
                  <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center', justifyContent: 'flex-end' }}>
                    {onLifecycleSuccess ? (
                      <InitiativeLifecycleActions
                        initiative={initiative}
                        onSuccess={onLifecycleSuccess}
                      />
                    ) : null}
                    {onEdit ? (
                      <Tooltip title="Edit initiative">
                        <IconButton
                          aria-label={`Edit initiative ${initiative.title}`}
                          onClick={(event) => {
                            event.stopPropagation()
                            onEdit(initiative)
                          }}
                          size="small"
                        >
                          <EditOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    ) : null}
                  </Stack>
                </TableCell>
              ) : null}
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
  onEdit?: (initiative: Initiative) => void
  onLifecycleSuccess?: (action: InitiativeLifecycleAction, updated: Initiative) => void
}

export function InitiativeCardList({
  initiatives,
  loading,
  onEdit,
  onLifecycleSuccess,
  showStatusColumn,
}: InitiativeCardListProps) {
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
                <Box sx={{ minWidth: 0, width: '100%' }}>
                  <TruncatedTextWithTooltip
                    maxLength={TEXT_DISPLAY_LIMITS.cardTitle}
                    text={initiative.title}
                    variant="subtitle1"
                  />
                  <Typography color="text.secondary" sx={{ mt: 0.5 }} variant="body2">
                    Expires {formatInitiativeDate(initiative.expiryDateUtc)}
                  </Typography>
                  {initiative.rewardDescription ? (
                    <Box sx={{ mt: 1 }}>
                      <TruncatedTextWithTooltip
                        color="text.secondary"
                        maxLength={TEXT_DISPLAY_LIMITS.cardReward}
                        text={initiative.rewardDescription}
                      />
                    </Box>
                  ) : null}
                  <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1, mt: 1 }}>
                    {showStatusColumn ? <InitiativeStatusChip status={initiative.status} /> : null}
                    <InitiativeExpiryBadge expiryDateUtc={initiative.expiryDateUtc} status={initiative.status} />
                  </Stack>
                </Box>
                <Stack direction="row" spacing={0.5} sx={{ flexShrink: 0 }}>
                  {onLifecycleSuccess ? (
                    <InitiativeLifecycleActions
                      initiative={initiative}
                      onSuccess={onLifecycleSuccess}
                    />
                  ) : null}
                  {onEdit ? (
                    <Tooltip title="Edit initiative">
                      <IconButton
                        aria-label={`Edit initiative ${initiative.title}`}
                        onClick={(event) => {
                          event.stopPropagation()
                          event.preventDefault()
                          onEdit(initiative)
                        }}
                        size="small"
                      >
                        <EditOutlinedIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  ) : null}
                  <ChevronRightOutlinedIcon color="action" />
                </Stack>
              </Stack>
            </CardContent>
          </CardActionArea>
        </Card>
      ))}
    </Stack>
  )
}
