import FilterAltOutlinedIcon from '@mui/icons-material/FilterAltOutlined'
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined'
import { Box, Button, Card, CardContent, InputAdornment, MenuItem, Stack, TextField } from '@mui/material'
import type { UserListQuery } from '../../types/users'

const USER_FILTER_LIMITS = {
  fullName: 200,
  employeeId: 64,
  email: 320,
} as const

interface UserFiltersProps {
  draft: UserListQuery
  onDraftChange: (draft: UserListQuery) => void
  onApply: () => void
  onClear: () => void
}

export function UserFilters({ draft, onDraftChange, onApply, onClear }: UserFiltersProps) {
  function updateField<K extends keyof UserListQuery>(field: K, value: UserListQuery[K]) {
    onDraftChange({ ...draft, [field]: value })
  }

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Stack spacing={2}>
          <Box
            sx={{
              display: 'grid',
              gap: 2,
              gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' },
            }}
          >
            <TextField
              label="Search by name"
              slotProps={{
                htmlInput: { maxLength: USER_FILTER_LIMITS.fullName },
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchOutlinedIcon color="action" />
                    </InputAdornment>
                  ),
                },
              }}
              onChange={(event) => updateField('fullName', event.target.value)}
              placeholder="Quick search"
              value={draft.fullName}
            />
            <TextField
              label="Employee ID"
              onChange={(event) => updateField('employeeId', event.target.value)}
              slotProps={{ htmlInput: { maxLength: USER_FILTER_LIMITS.employeeId } }}
              value={draft.employeeId}
            />
            <TextField
              label="Email"
              onChange={(event) => updateField('email', event.target.value)}
              slotProps={{ htmlInput: { maxLength: USER_FILTER_LIMITS.email } }}
              type="email"
              value={draft.email}
            />
            <TextField
              label="Role"
              onChange={(event) => updateField('role', event.target.value as UserListQuery['role'])}
              select
              value={draft.role}
            >
              <MenuItem value="">All roles</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
              <MenuItem value="EMPLOYEE">Employee</MenuItem>
            </TextField>
            <TextField
              label="Status"
              onChange={(event) => updateField('active', event.target.value as UserListQuery['active'])}
              select
              value={draft.active}
            >
              <MenuItem value="">All statuses</MenuItem>
              <MenuItem value="true">Active</MenuItem>
              <MenuItem value="false">Inactive</MenuItem>
            </TextField>
          </Box>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
            <Button onClick={onApply} startIcon={<FilterAltOutlinedIcon />} variant="contained">
              Apply filters
            </Button>
            <Button onClick={onClear} variant="outlined">
              Clear
            </Button>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  )
}
