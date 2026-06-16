import { useEffect, useState } from 'react'
import { Avatar } from '@mui/material'
import { profileApi } from '../../api/profileApi'
import { getProfileInitials } from '../../utils/profileInitials'

interface ProfileAvatarProps {
  fullName: string
  hasAvatar?: boolean
  avatarCacheKey?: string
  size?: number
}

export function ProfileAvatar({
  fullName,
  hasAvatar = false,
  avatarCacheKey,
  size = 96,
}: ProfileAvatarProps) {
  const [avatarSrc, setAvatarSrc] = useState<string | null>(null)
  const initials = getProfileInitials(fullName)

  useEffect(() => {
    if (!hasAvatar) {
      setAvatarSrc(null)
      return undefined
    }

    let mounted = true
    let objectUrl: string | null = null

    async function loadAvatar() {
      try {
        const blob = await profileApi.getAvatarBlob(avatarCacheKey)
        objectUrl = URL.createObjectURL(blob)
        if (mounted) {
          setAvatarSrc(objectUrl)
        }
      } catch {
        if (mounted) {
          setAvatarSrc(null)
        }
      }
    }

    loadAvatar()

    return () => {
      mounted = false
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl)
      }
    }
  }, [avatarCacheKey, hasAvatar])

  return (
    <Avatar
      alt={fullName}
      aria-label={`Profile avatar for ${fullName}`}
      src={avatarSrc ?? undefined}
      sx={{ height: size, width: size, fontSize: size * 0.35 }}
    >
      {avatarSrc ? null : initials}
    </Avatar>
  )
}
