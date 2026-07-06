import { FormControl, InputLabel, MenuItem, Select, Stack, Switch, FormControlLabel } from '@mui/material'
import type { ProjectListQuery } from '../../types/projects'
import { PROJECT_ACCESS_LABELS, PROJECT_STATUS_LABELS } from '../../types/projects'
import { PROJECT_MESSAGES } from './projectMessages'

interface ProjectListFiltersProps {
  query: ProjectListQuery
  onChange: (query: ProjectListQuery) => void
  isAdmin: boolean
}

export function ProjectListFilters({ isAdmin, onChange, query }: ProjectListFiltersProps) {
  return (
    <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' } }}>
      <FormControl size="small" sx={{ minWidth: 160 }}>
        <InputLabel id="project-status-filter-label">{PROJECT_MESSAGES.status}</InputLabel>
        <Select
          label={PROJECT_MESSAGES.status}
          labelId="project-status-filter-label"
          onChange={(event) =>
            onChange({
              ...query,
              page: 0,
              status: event.target.value as ProjectListQuery['status'],
            })
          }
          value={query.status}
        >
          <MenuItem value="">All statuses</MenuItem>
          {Object.entries(PROJECT_STATUS_LABELS).map(([value, label]) => (
            <MenuItem key={value} value={value}>
              {label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl size="small" sx={{ minWidth: 180 }}>
        <InputLabel id="project-access-filter-label">{PROJECT_MESSAGES.visibility}</InputLabel>
        <Select
          label={PROJECT_MESSAGES.visibility}
          labelId="project-access-filter-label"
          onChange={(event) =>
            onChange({
              ...query,
              page: 0,
              accessType: event.target.value as ProjectListQuery['accessType'],
            })
          }
          value={query.accessType}
        >
          <MenuItem value="">All visibility</MenuItem>
          {Object.entries(PROJECT_ACCESS_LABELS).map(([value, label]) => (
            <MenuItem key={value} value={value}>
              {label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControlLabel
        control={
          <Switch
            checked={query.assigned}
            onChange={(event) => onChange({ ...query, page: 0, assigned: event.target.checked })}
          />
        }
        label={PROJECT_MESSAGES.assignedFilter}
      />
      {isAdmin ? (
        <FormControlLabel
          control={
            <Switch
              checked={query.includeArchived}
              onChange={(event) => onChange({ ...query, page: 0, includeArchived: event.target.checked })}
            />
          }
          label={PROJECT_MESSAGES.includeArchived}
        />
      ) : null}
    </Stack>
  )
}
