import { useCallback, useEffect, useState } from 'react'
import { Alert, Box, CircularProgress } from '@mui/material'
import { profileApi } from '../../api/profileApi'
import { tokenStorage } from '../../api/httpClient'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { ProfileEditForm } from '../../components/profile/ProfileEditForm'
import { PROFILE_MESSAGES } from '../../components/profile/profileMessages'
import { ProfileViewSection } from '../../components/profile/ProfileViewSection'
import type { UserManagementNotification } from '../../components/users/UserManagementSnackbar'
import { UserManagementSnackbar } from '../../components/users/UserManagementSnackbar'
import type { Profile } from '../../types/profile'
import { resolveApiError } from '../../utils/apiErrors'

export function ProfilePage() {
  const { refreshProfile } = useAuth()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [notification, setNotification] = useState<UserManagementNotification | null>(null)

  const loadProfile = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await profileApi.get()
      setProfile(data)
    } catch (loadError) {
      setProfile(null)
      setError(resolveApiError(loadError, 'Unable to load profile. Please try again.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadProfile()
  }, [loadProfile])

  async function handleUpdateSuccess(updatedProfile: Profile, accessToken?: string | null) {
    if (accessToken) {
      tokenStorage.set(accessToken)
    }
    setProfile(updatedProfile)
    setIsEditing(false)
    setNotification({ message: PROFILE_MESSAGES.updateSuccess, severity: 'success' })
    try {
      await refreshProfile()
    } catch {
      setNotification({ message: PROFILE_MESSAGES.updateError, severity: 'error' })
    }
  }

  return (
    <Box>
      <PageHeader
        description="View and update your account information."
        title="My Profile"
      />

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress aria-label="Loading profile" />
        </Box>
      ) : null}

      {!loading && error ? <Alert severity="error">{error}</Alert> : null}

      {!loading && !error && profile && !isEditing ? (
        <ProfileViewSection onEdit={() => setIsEditing(true)} profile={profile} />
      ) : null}

      {!loading && !error && profile && isEditing ? (
        <ProfileEditForm
          onCancel={() => setIsEditing(false)}
          onSuccess={handleUpdateSuccess}
          profile={profile}
        />
      ) : null}

      <UserManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </Box>
  )
}
