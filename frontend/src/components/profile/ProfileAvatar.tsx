import { Avatar } from '@mui/material'
import { getProfileInitials } from '../../utils/profileInitials'

interface ProfileAvatarProps {
  fullName: string
  hasAvatar?: boolean
  size?: number
}

export function ProfileAvatar({ fullName, size = 96 }: ProfileAvatarProps) {
  const initials = getProfileInitials(fullName)

  return (
    <Avatar
      alt={fullName}
      aria-label={`Profile avatar for ${fullName}`}
      sx={{ height: size, width: size, fontSize: size * 0.35 }}
    >
      {initials}
    </Avatar>
  )
}
