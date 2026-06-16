import { useEffect, useState } from 'react'
import { Alert, Box, CircularProgress } from '@mui/material'
import { profileApi } from '../../api/profileApi'
import { PageHeader } from '../../components/common/PageHeader'
import { ProfileViewSection } from '../../components/profile/ProfileViewSection'
import type { Profile } from '../../types/profile'
import { resolveApiError } from '../../utils/apiErrors'

export function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true

    async function loadProfile() {
      setLoading(true)
      setError(null)
      try {
        const data = await profileApi.get()
        if (mounted) {
          setProfile(data)
        }
      } catch (loadError) {
        if (mounted) {
          setProfile(null)
          setError(resolveApiError(loadError, 'Unable to load profile. Please try again.'))
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    loadProfile()
    return () => {
      mounted = false
    }
  }, [])

  return (
    <Box>
      <PageHeader
        description="View your account information. Profile editing will be available in a later release."
        title="My Profile"
      />

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress aria-label="Loading profile" />
        </Box>
      ) : null}

      {!loading && error ? <Alert severity="error">{error}</Alert> : null}

      {!loading && !error && profile ? <ProfileViewSection profile={profile} /> : null}
    </Box>
  )
}
