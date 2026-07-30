import ArrowForwardOutlinedIcon from '@mui/icons-material/ArrowForwardOutlined'
import {
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Skeleton,
  Stack,
  Typography,
} from '@mui/material'
import type { SvgIconComponent } from '@mui/icons-material'
import { Link as RouterLink } from 'react-router-dom'

export interface DashboardQuickAction {
  id: string
  label: string
  description: string
  href: string
  icon: SvgIconComponent
}

interface DashboardQuickActionsCardProps {
  actions: DashboardQuickAction[]
  loading?: boolean
}

export function DashboardQuickActionsCard({ actions, loading = false }: DashboardQuickActionsCardProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent sx={{ p: 2.5 }}>
        <Typography component="h3" sx={{ fontWeight: 700, mb: 2 }} variant="h6">
          Quick Actions
        </Typography>
        {loading ? (
          <Stack spacing={1}>
            <Skeleton height={56} variant="rounded" />
            <Skeleton height={56} variant="rounded" />
            <Skeleton height={56} variant="rounded" />
          </Stack>
        ) : (
          <Stack spacing={1}>
            {actions.map((action) => {
              const Icon = action.icon
              return (
                <Button
                  component={RouterLink}
                  endIcon={<ArrowForwardOutlinedIcon fontSize="small" />}
                  key={action.id}
                  sx={{
                    borderColor: 'divider',
                    justifyContent: 'space-between',
                    px: 2,
                    py: 1.25,
                    textAlign: 'left',
                  }}
                  to={action.href}
                  variant="outlined"
                >
                  <Box sx={{ alignItems: 'center', display: 'flex', gap: 1.5, minWidth: 0 }}>
                    <Box
                      sx={{
                        alignItems: 'center',
                        bgcolor: 'action.hover',
                        borderRadius: 1.5,
                        color: 'primary.main',
                        display: 'flex',
                        flexShrink: 0,
                        height: 36,
                        justifyContent: 'center',
                        width: 36,
                      }}
                    >
                      <Icon fontSize="small" />
                    </Box>
                    <Box sx={{ minWidth: 0 }}>
                      <Typography sx={{ fontWeight: 600 }} variant="body2">
                        {action.label}
                      </Typography>
                      <Typography color="text.secondary" noWrap variant="caption">
                        {action.description}
                      </Typography>
                    </Box>
                  </Box>
                </Button>
              )
            })}
          </Stack>
        )}
      </CardContent>
    </Card>
  )
}

interface DashboardActivityCardProps {
  items: Array<{
    id: string
    title: string
    description: string
    timestamp: string
    href?: string
  }>
  loading?: boolean
  emptyText: string
}

export function DashboardActivityCard({ emptyText, items, loading = false }: DashboardActivityCardProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent sx={{ p: 2.5 }}>
        <Typography component="h3" sx={{ fontWeight: 700, mb: 2 }} variant="h6">
          Recent Activity
        </Typography>
        {loading ? (
          <Stack spacing={1}>
            <Skeleton height={48} />
            <Skeleton height={48} />
            <Skeleton height={48} />
          </Stack>
        ) : items.length === 0 ? (
          <Typography color="text.secondary" variant="body2">
            {emptyText}
          </Typography>
        ) : (
          <List disablePadding>
            {items.map((item, index) => {
              const content = (
                <ListItemText
                  primary={item.title}
                  secondary={
                    <>
                      <Typography color="text.secondary" component="span" noWrap sx={{ display: 'block' }} variant="body2">
                        {item.description}
                      </Typography>
                      <Typography color="text.secondary" component="span" sx={{ display: 'block', mt: 0.25 }} variant="caption">
                        {formatDashboardTimestamp(item.timestamp)}
                      </Typography>
                    </>
                  }
                  slotProps={{
                    primary: { sx: { fontWeight: 600 } },
                  }}
                />
              )

              return (
                <Box key={item.id}>
                  {item.href ? (
                    <ListItemButton component={RouterLink} disableGutters sx={{ px: 0, py: 1 }} to={item.href}>
                      {content}
                    </ListItemButton>
                  ) : (
                    <ListItem disableGutters sx={{ px: 0, py: 1 }}>
                      {content}
                    </ListItem>
                  )}
                  {index < items.length - 1 ? <Divider /> : null}
                </Box>
              )
            })}
          </List>
        )}
      </CardContent>
    </Card>
  )
}

interface DashboardRankCardProps {
  rank: number | null
  totalApprovedCertifications: number
  loading?: boolean
}

export function DashboardRankCard({ loading = false, rank, totalApprovedCertifications }: DashboardRankCardProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent sx={{ p: 2.5 }}>
        <Typography component="h3" sx={{ fontWeight: 700, mb: 2 }} variant="h6">
          Leaderboard Rank
        </Typography>
        {loading ? (
          <Stack spacing={1}>
            <Skeleton height={56} variant="rounded" />
            <Skeleton height={24} width="70%" />
          </Stack>
        ) : (
          <Stack spacing={1.5}>
            <Typography sx={{ fontWeight: 700, lineHeight: 1 }} variant="h3">
              {rank ? `#${rank}` : '--'}
            </Typography>
            <Typography color="text.secondary" variant="body2">
              {totalApprovedCertifications
                ? `${totalApprovedCertifications} approved certification${totalApprovedCertifications === 1 ? '' : 's'}`
                : 'No approved certifications yet'}
            </Typography>
            <Button component={RouterLink} size="small" sx={{ alignSelf: 'flex-start' }} to="/leaderboards/global" variant="text">
              View leaderboard
            </Button>
          </Stack>
        )}
      </CardContent>
    </Card>
  )
}

interface DashboardNotificationsCardProps {
  notifications: Array<{
    id: string
    title: string
    message: string
    createdAtUtc: string
    read: boolean
    actionPath?: string | null
  }>
  loading?: boolean
  emptyText: string
  unreadCount?: number
}

export function DashboardNotificationsCard({
  emptyText,
  loading = false,
  notifications,
  unreadCount = 0,
}: DashboardNotificationsCardProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent sx={{ p: 2.5 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Typography component="h3" sx={{ fontWeight: 700 }} variant="h6">
            Recent Notifications
          </Typography>
          {!loading && unreadCount > 0 ? (
            <Typography color="primary" sx={{ fontWeight: 600 }} variant="caption">
              {unreadCount} unread
            </Typography>
          ) : null}
        </Stack>
        {loading ? (
          <Stack spacing={1}>
            <Skeleton height={48} />
            <Skeleton height={48} />
            <Skeleton height={48} />
          </Stack>
        ) : notifications.length === 0 ? (
          <Typography color="text.secondary" variant="body2">
            {emptyText}
          </Typography>
        ) : (
          <List disablePadding>
            {notifications.map((notification, index) => (
              <Box key={notification.id}>
                <ListItemButton
                  component={RouterLink}
                  disableGutters
                  sx={{
                    bgcolor: notification.read ? 'transparent' : 'action.hover',
                    borderRadius: 1,
                    mb: 0.5,
                    px: 1,
                    py: 1,
                  }}
                  to={notification.actionPath ?? '/notifications'}
                >
                  <ListItemIcon sx={{ minWidth: 32 }}>
                    <Box
                      sx={{
                        bgcolor: notification.read ? 'transparent' : 'primary.main',
                        borderRadius: '50%',
                        height: 8,
                        width: 8,
                      }}
                    />
                  </ListItemIcon>
                  <ListItemText
                    primary={notification.title}
                    secondary={
                      <>
                        <Typography color="text.secondary" component="span" noWrap sx={{ display: 'block' }} variant="body2">
                          {notification.message}
                        </Typography>
                        <Typography color="text.secondary" component="span" sx={{ display: 'block', mt: 0.25 }} variant="caption">
                          {formatDashboardTimestamp(notification.createdAtUtc)}
                        </Typography>
                      </>
                    }
                    slotProps={{
                      primary: { sx: { fontWeight: notification.read ? 500 : 700 } },
                    }}
                  />
                </ListItemButton>
                {index < notifications.length - 1 ? <Divider sx={{ my: 0.5 }} /> : null}
              </Box>
            ))}
          </List>
        )}
        <Button component={RouterLink} size="small" sx={{ mt: 2 }} to="/notifications" variant="text">
          Open inbox
        </Button>
      </CardContent>
    </Card>
  )
}

function formatDashboardTimestamp(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  const now = Date.now()
  const diffMs = now - date.getTime()
  const minute = 60_000
  const hour = 60 * minute
  const day = 24 * hour

  if (diffMs < minute) {
    return 'Just now'
  }
  if (diffMs < hour) {
    const minutes = Math.floor(diffMs / minute)
    return `${minutes} minute${minutes === 1 ? '' : 's'} ago`
  }
  if (diffMs < day) {
    const hours = Math.floor(diffMs / hour)
    return `${hours} hour${hours === 1 ? '' : 's'} ago`
  }
  if (diffMs < 7 * day) {
    const days = Math.floor(diffMs / day)
    return `${days} day${days === 1 ? '' : 's'} ago`
  }

  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(date)
}
