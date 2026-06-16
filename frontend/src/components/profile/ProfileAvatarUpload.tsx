import { useRef, useState } from 'react'
import type { ChangeEvent } from 'react'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import PhotoCameraOutlinedIcon from '@mui/icons-material/PhotoCameraOutlined'
import {
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'
import { profileApi } from '../../api/profileApi'
import type { Profile } from '../../types/profile'
import { resolveApiError } from '../../utils/apiErrors'
import { AVATAR_ACCEPT, AVATAR_MAX_SIZE_BYTES, PROFILE_MESSAGES } from './profileMessages'

interface ProfileAvatarUploadProps {
  profile: Profile
  disabled?: boolean
  onUpdated: (profile: Profile) => void
  onError: (message: string) => void
}

export function ProfileAvatarUpload({
  profile,
  disabled = false,
  onUpdated,
  onError,
}: ProfileAvatarUploadProps) {
  const inputRef = useRef<HTMLInputElement | null>(null)
  const [uploading, setUploading] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)

  async function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) {
      return
    }

    if (!isAllowedAvatarFile(file)) {
      onError(PROFILE_MESSAGES.avatarValidationError)
      return
    }

    setUploading(true)
    try {
      const updatedProfile = await profileApi.uploadAvatar(file)
      onUpdated(updatedProfile)
    } catch (error) {
      onError(resolveApiError(error, PROFILE_MESSAGES.avatarUploadError))
    } finally {
      setUploading(false)
    }
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await profileApi.deleteAvatar()
      const updatedProfile = await profileApi.get()
      onUpdated(updatedProfile)
      setDeleteOpen(false)
    } catch (error) {
      onError(resolveApiError(error, PROFILE_MESSAGES.avatarDeleteError))
    } finally {
      setDeleting(false)
    }
  }

  const busy = uploading || deleting

  return (
    <>
      <Stack direction="row" spacing={1} sx={{ justifyContent: 'center' }}>
        <input
          accept={AVATAR_ACCEPT}
          hidden
          onChange={handleFileChange}
          ref={inputRef}
          type="file"
        />
        <Button
          disabled={disabled || busy}
          onClick={() => inputRef.current?.click()}
          startIcon={uploading ? <CircularProgress color="inherit" size={18} /> : <PhotoCameraOutlinedIcon />}
          variant="outlined"
        >
          {profile.hasAvatar ? 'Replace Photo' : 'Upload Photo'}
        </Button>
        {profile.hasAvatar ? (
          <Button
            color="error"
            disabled={disabled || busy}
            onClick={() => setDeleteOpen(true)}
            startIcon={deleting ? <CircularProgress color="inherit" size={18} /> : <DeleteOutlinedIcon />}
            variant="outlined"
          >
            Delete Photo
          </Button>
        ) : null}
      </Stack>

      <Dialog fullWidth maxWidth="xs" onClose={busy ? undefined : () => setDeleteOpen(false)} open={deleteOpen}>
        <DialogTitle>Remove avatar?</DialogTitle>
        <DialogContent>
          <Typography color="text.secondary" variant="body2">
            Your profile photo will be removed and initials will be shown instead.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button disabled={busy} onClick={() => setDeleteOpen(false)}>
            Cancel
          </Button>
          <Button color="error" disabled={busy} onClick={handleDelete} variant="contained">
            {deleting ? <CircularProgress color="inherit" size={24} /> : 'Delete Photo'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

function isAllowedAvatarFile(file: File) {
  if (file.size > AVATAR_MAX_SIZE_BYTES) {
    return false
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (allowedTypes.includes(file.type)) {
    return true
  }

  const extension = file.name.slice(file.name.lastIndexOf('.')).toLowerCase()
  return ['.jpg', '.jpeg', '.png', '.webp'].includes(extension)
}
