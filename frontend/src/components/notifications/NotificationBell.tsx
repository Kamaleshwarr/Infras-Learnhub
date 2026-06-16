import NotificationsOutlinedIcon from '@mui/icons-material/NotificationsOutlined'
import { Badge, IconButton, Tooltip } from '@mui/material'
import { useState } from 'react'
import { useUnreadNotificationCount } from '../../hooks/useUnreadNotificationCount'
import { NotificationMenu } from './NotificationMenu'

export function NotificationBell() {
  const { unreadCount, refresh, clearUnreadCount } = useUnreadNotificationCount()
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
        onClose={() => {
          setAnchorEl(null)
          void refresh()
        }}
        onUnreadCountChange={(nextCount) => {
          if (nextCount === 0) {
            clearUnreadCount()
            return
          }
          void refresh()
        }}
        open={open}
      />
    </>
  )
}
