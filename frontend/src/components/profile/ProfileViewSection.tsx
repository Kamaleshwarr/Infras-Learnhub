import { Box, Button, Card, CardContent, Grid, Stack, TextField, Typography } from '@mui/material'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import { UserStatusChip } from '../users/UserStatusChip'
import { readOnlyTextFieldSlotProps } from '../common/readOnlyTextField'
import { WrappingText } from '../common/WrappingText'
import { ProfileAvatar } from './ProfileAvatar'
import { ProfileAvatarUpload } from './ProfileAvatarUpload'
import type { Profile } from '../../types/profile'

interface ProfileViewSectionProps {
  profile: Profile
  onEdit?: () => void
  onChangePassword?: () => void
  showChangePassword?: boolean
  onAvatarUpdated?: (profile: Profile) => void
  onAvatarError?: (message: string) => void
  avatarBusy?: boolean
}

export function ProfileViewSection({
  profile,
  onEdit,
  onChangePassword,
  showChangePassword = false,
  onAvatarUpdated,
  onAvatarError,
  avatarBusy = false,
}: ProfileViewSectionProps) {
  return (
    <Card sx={{ minWidth: 0 }}>
      <CardContent sx={{ p: { xs: 2, md: 3 } }}>
        <Stack spacing={3}>
          <Stack spacing={2} sx={{ alignItems: 'center' }}>
            <ProfileAvatar
              avatarCacheKey={profile.updatedAtUtc}
              fullName={profile.fullName}
              hasAvatar={profile.hasAvatar}
            />
            <Box sx={{ minWidth: 0, textAlign: 'center', width: '100%' }}>
              <WrappingText component="h2" variant="h5">
                {profile.fullName}
              </WrappingText>
              <WrappingText color="text.secondary" variant="body2">
                {profile.email}
              </WrappingText>
            </Box>
            {onAvatarUpdated && onAvatarError ? (
              <ProfileAvatarUpload
                disabled={avatarBusy}
                onError={onAvatarError}
                onUpdated={onAvatarUpdated}
                profile={profile}
              />
            ) : null}
          </Stack>

          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Full Name"
                slotProps={readOnlyTextFieldSlotProps}
                value={profile.fullName}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Email"
                slotProps={readOnlyTextFieldSlotProps}
                value={profile.email}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Employee ID"
                slotProps={readOnlyTextFieldSlotProps}
                value={profile.employeeId}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Role"
                slotProps={readOnlyTextFieldSlotProps}
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

          {onEdit || showChangePassword ? (
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
              {showChangePassword && onChangePassword ? (
                <Button onClick={onChangePassword} startIcon={<LockOutlinedIcon />} variant="outlined">
                  Change Password
                </Button>
              ) : null}
              {onEdit ? (
                <Button onClick={onEdit} startIcon={<EditOutlinedIcon />} variant="outlined">
                  Edit Profile
                </Button>
              ) : null}
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
