import NotificationsOutlinedIcon from '@mui/icons-material/NotificationsOutlined'
import { Badge, IconButton, Tooltip } from '@mui/material'
import { useState } from 'react'
import { useNotifications } from '../../notifications/useNotifications'
import { NotificationMenu } from './NotificationMenu'

export function NotificationBell() {
  const { unreadCount } = useNotifications()
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null)
  const open = Boolean(anchorEl)

  return (
    <>
      <Tooltip title="Notifications">
        <IconButton aria-label="Notifications" color="primary" onClick={(event) => setAnchorEl(event.currentTarget)}>
          <Badge badgeContent={unreadCount > 0 ? (unreadCount > 99 ? '99+' : unreadCount) : undefined} color="error">
            <NotificationsOutlinedIcon />
          </Badge>
        </IconButton>
      </Tooltip>
      <NotificationMenu
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
        open={open}
      />
    </>
  )
}
