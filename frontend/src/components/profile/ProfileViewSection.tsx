import { Box, Button, Card, CardContent, Grid, Stack, TextField, Typography } from '@mui/material'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import { UserStatusChip } from '../users/UserStatusChip'
import { ProfileAvatar } from './ProfileAvatar'
import type { Profile } from '../../types/profile'

interface ProfileViewSectionProps {
  profile: Profile
  onEdit?: () => void
}

export function ProfileViewSection({ profile, onEdit }: ProfileViewSectionProps) {
  return (
    <Card>
      <CardContent sx={{ p: { xs: 2, md: 3 } }}>
        <Stack spacing={3}>
          <Stack spacing={2} sx={{ alignItems: 'center' }}>
            <ProfileAvatar fullName={profile.fullName} hasAvatar={profile.hasAvatar} />
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="h5">{profile.fullName}</Typography>
              <Typography color="text.secondary" variant="body2">
                {profile.email}
              </Typography>
            </Box>
          </Stack>

          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Full Name"
                slotProps={{ input: { readOnly: true } }}
                value={profile.fullName}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Email"
                slotProps={{ input: { readOnly: true } }}
                value={profile.email}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Employee ID"
                slotProps={{ input: { readOnly: true } }}
                value={profile.employeeId}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Role"
                slotProps={{ input: { readOnly: true } }}
                value={profile.role}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <Stack spacing={1}>
                <Typography color="text.secondary" variant="body2">
                  Status
                </Typography>
                <Box>
                  <UserStatusChip active={profile.active} />
                </Box>
              </Stack>
            </Grid>
          </Grid>

          <Typography color="text.secondary" variant="caption">
            Created {formatTimestamp(profile.createdAtUtc)} · Updated {formatTimestamp(profile.updatedAtUtc)}
          </Typography>

          {onEdit ? (
            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button onClick={onEdit} startIcon={<EditOutlinedIcon />} variant="outlined">
                Edit Profile
              </Button>
            </Box>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  )
}

function formatTimestamp(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(
    new Date(value),
  )
}
