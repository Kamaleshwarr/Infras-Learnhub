import {
  Box,
  Button,
  CircularProgress,
  Divider,
  List,
  ListItemButton,
  ListItemText,
  Menu,
  Typography,
} from '@mui/material'
import { useCallback, useState } from 'react'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { notificationsApi } from '../../api/notificationsApi'
import { useNotifications } from '../../notifications/useNotifications'
import type { Notification } from '../../types/notifications'
import { resolveApiError } from '../../utils/apiErrors'

interface NotificationListItemProps {
  notification: Notification
  onNavigate: (notification: Notification) => void
}

export function NotificationListItem({ notification, onNavigate }: NotificationListItemProps) {
  return (
    <ListItemButton
      alignItems="flex-start"
      onClick={() => onNavigate(notification)}
      sx={{
        bgcolor: notification.read ? 'transparent' : 'action.hover',
        py: 1.5,
      }}
    >
      <ListItemText
        primary={
          <Typography sx={{ fontWeight: notification.read ? 400 : 600 }} variant="body2">
            {notification.title}
          </Typography>
        }
        secondary={
          <>
            <Typography color="text.secondary" component="span" sx={{ display: 'block' }} variant="body2">
              {notification.message}
            </Typography>
            <Typography color="text.secondary" component="span" sx={{ display: 'block', mt: 0.5 }} variant="caption">
              {formatNotificationTimestamp(notification.createdAtUtc)}
            </Typography>
          </>
        }
      />
    </ListItemButton>
  )
}

interface NotificationMenuProps {
  anchorEl: HTMLElement | null
  open: boolean
  onClose: () => void
}

export function NotificationMenu({ anchorEl, open, onClose }: NotificationMenuProps) {
  const navigate = useNavigate()
  const { refresh } = useNotifications()
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [markingAllRead, setMarkingAllRead] = useState(false)

  const loadNotifications = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await notificationsApi.list({ page: 0, size: 10, sort: 'createdAt,desc' })
      setNotifications(response.content)
    } catch (loadError) {
      setError(resolveApiError(loadError, 'Unable to load notifications.'))
    } finally {
      setLoading(false)
    }
  }, [])

  const handleMenuEntered = useCallback(() => {
    void loadNotifications()
  }, [loadNotifications])

  const handleNotificationClick = useCallback(
    async (notification: Notification) => {
      try {
        if (!notification.read) {
          await notificationsApi.markRead(notification.id)
          await refresh()
        }
      } catch {
        // Navigation should still proceed when mark-read fails.
      }

      onClose()
      if (notification.actionPath) {
        navigate(notification.actionPath)
      }
    },
    [navigate, onClose, refresh],
  )

  const handleMarkAllRead = useCallback(async () => {
    setMarkingAllRead(true)
    setError(null)
    try {
      await notificationsApi.markAllRead()
      setNotifications((current) =>
        current.map((notification) => ({
          ...notification,
          read: true,
          readAtUtc: notification.readAtUtc ?? new Date().toISOString(),
        })),
      )
      await refresh()
    } catch (markError) {
      setError(resolveApiError(markError, 'Unable to mark notifications as read.'))
    } finally {
      setMarkingAllRead(false)
    }
  }, [refresh])

  return (
    <Menu
      anchorEl={anchorEl}
      anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      onClose={onClose}
      onTransitionEnter={handleMenuEntered}
      open={open}
      slotProps={{
        list: {
          'aria-label': 'Notifications',
          sx: { p: 0, width: 360, maxWidth: '100vw' },
        },
      }}
      transformOrigin={{ horizontal: 'right', vertical: 'top' }}
    >
      <Box sx={{ px: 2, py: 1.5, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="subtitle1">Notifications</Typography>
        <Button disabled={markingAllRead || loading || notifications.every((item) => item.read)} onClick={() => void handleMarkAllRead()} size="small">
          Mark all read
        </Button>
      </Box>
      <Divider />
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress size={24} />
        </Box>
      ) : error ? (
        <Box sx={{ px: 2, py: 2 }}>
          <Typography color="error" variant="body2">
            {error}
          </Typography>
        </Box>
      ) : notifications.length === 0 ? (
        <Box sx={{ px: 2, py: 3 }}>
          <Typography color="text.secondary" variant="body2">
            No notifications yet.
          </Typography>
        </Box>
      ) : (
        <List disablePadding sx={{ maxHeight: 360, overflowY: 'auto' }}>
          {notifications.map((notification) => (
            <NotificationListItem key={notification.id} notification={notification} onNavigate={(item) => void handleNotificationClick(item)} />
          ))}
        </List>
      )}
      <Divider />
      <Box sx={{ px: 2, py: 1.5 }}>
        <Button component={RouterLink} fullWidth onClick={onClose} to="/notifications" variant="text">
          View all notifications
        </Button>
      </Box>
    </Menu>
  )
}

function formatNotificationTimestamp(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}
