import { Avatar } from '@mui/material'
import { getProfileInitials } from '../../utils/profileInitials'

interface LeaderboardAvatarProps {
  fullName: string
  size?: number
}

export function LeaderboardAvatar({ fullName, size = 40 }: LeaderboardAvatarProps) {
  return (
    <Avatar
      alt={fullName}
      aria-hidden
      sx={{ height: size, width: size, fontSize: size * 0.38 }}
    >
      {getProfileInitials(fullName)}
    </Avatar>
  )
}
