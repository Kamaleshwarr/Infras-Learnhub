import ChevronRightOutlinedIcon from '@mui/icons-material/ChevronRightOutlined'
import TuneOutlinedIcon from '@mui/icons-material/TuneOutlined'
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
import type { Technology, TechnologyLifecycleAction } from '../../types/learn'
import { SortableTableHead, type SortableColumn } from '../common/SortableTableHead'
import { TruncatedTextWithTooltip } from '../common/TruncatedTextWithTooltip'
import { fixedTableSx, TEXT_DISPLAY_LIMITS } from '../common/textDisplay'
import { TechnologyCategoryChip, TechnologyDifficultyChip } from './TechnologyMetaChips'
import { TechnologyLifecycleActions } from './TechnologyLifecycleActions'
import { TechnologyStatusChip } from './TechnologyStatusChip'

interface TechnologyTableProps {
  technologies: Technology[]
  loading: boolean
  sort: string
  showStatusColumn: boolean
  onSort: (property: string) => void
  onCurate?: (technology: Technology) => void
  onLifecycleSuccess?: (action: TechnologyLifecycleAction) => void
}

const BASE_COLUMNS: SortableColumn[] = [
  { id: 'name', label: 'Name', sortable: true },
  { id: 'category', label: 'Category', sortable: true },
  { id: 'difficulty', label: 'Difficulty', sortable: true },
]

const STATUS_COLUMN: SortableColumn = { id: 'status', label: 'Status', sortable: true }
const ACTIONS_COLUMN: SortableColumn = { id: 'actions', align: 'right', label: 'Actions' }

function buildColumns(showStatusColumn: boolean, showActionsColumn: boolean) {
  const columns = showStatusColumn ? [...BASE_COLUMNS, STATUS_COLUMN] : [...BASE_COLUMNS]
  if (showActionsColumn) {
    columns.push(ACTIONS_COLUMN)
  }
  return columns
}

export function TechnologyTable({
  technologies,
  loading,
  sort,
  showStatusColumn,
  onSort,
  onCurate,
  onLifecycleSuccess,
}: TechnologyTableProps) {
  const navigate = useNavigate()
  const showActionsColumn = Boolean(onCurate || onLifecycleSuccess)
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
          {technologies.map((technology) => (
            <TableRow
              hover
              key={technology.id}
              onClick={
                showActionsColumn
                  ? undefined
                  : () => navigate(`/learn/technologies/${technology.id}`)
              }
              sx={showActionsColumn ? undefined : { cursor: 'pointer' }}
            >
              <TableCell>
                <Stack spacing={0.5}>
                  <Typography sx={{ fontWeight: 600 }}>{technology.name}</Typography>
                  <Typography color="text.secondary" variant="body2">
                    {technology.shortName}
                  </Typography>
                </Stack>
              </TableCell>
              <TableCell>
                <TechnologyCategoryChip category={technology.category} />
              </TableCell>
              <TableCell>
                <TechnologyDifficultyChip difficulty={technology.difficulty} />
              </TableCell>
              {showStatusColumn ? (
                <TableCell>
                  <TechnologyStatusChip status={technology.status} />
                </TableCell>
              ) : null}
              {showActionsColumn ? (
                <TableCell align="right">
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'flex-end' }}>
                    {onLifecycleSuccess ? (
                      <TechnologyLifecycleActions
                        onError={() => undefined}
                        onSuccess={(action) => onLifecycleSuccess(action)}
                        technology={technology}
                      />
                    ) : null}
                    {onCurate ? (
                      <Tooltip title="Curate">
                        <IconButton aria-label={`Curate ${technology.name}`} onClick={() => onCurate(technology)}>
                          <TuneOutlinedIcon />
                        </IconButton>
                      </Tooltip>
                    ) : null}
                    <Tooltip title="View details">
                      <IconButton
                        aria-label={`View ${technology.name}`}
                        onClick={() => navigate(`/learn/technologies/${technology.id}`)}
                      >
                        <ChevronRightOutlinedIcon />
                      </IconButton>
                    </Tooltip>
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

interface TechnologyCardListProps {
  technologies: Technology[]
  loading: boolean
}

export function TechnologyCardList({ technologies, loading }: TechnologyCardListProps) {
  const navigate = useNavigate()

  if (loading) {
    return (
      <Stack spacing={2}>
        {Array.from({ length: 3 }).map((_, index) => (
          <Skeleton height={120} key={index} variant="rounded" />
        ))}
      </Stack>
    )
  }

  return (
    <Stack spacing={2}>
      {technologies.map((technology) => (
        <Card key={technology.id} variant="outlined">
          <CardActionArea onClick={() => navigate(`/learn/technologies/${technology.id}`)}>
            <CardContent>
              <Stack spacing={1}>
                <Typography variant="h6">{technology.name}</Typography>
                <TruncatedTextWithTooltip
                  maxLength={TEXT_DISPLAY_LIMITS.cardTitle}
                  text={technology.description ?? technology.shortName}
                />
                <Stack direction="row" spacing={1}>
                  <TechnologyCategoryChip category={technology.category} />
                  <TechnologyDifficultyChip difficulty={technology.difficulty} />
                </Stack>
              </Stack>
            </CardContent>
          </CardActionArea>
        </Card>
      ))}
    </Stack>
  )
}
