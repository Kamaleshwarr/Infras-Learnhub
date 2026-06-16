import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, CircularProgress, List, Paper, Tab, Tabs } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { notificationsApi } from '../../api/notificationsApi'
import { useNotifications } from '../../notifications/useNotifications'
import { NotificationListItem } from '../../components/notifications/NotificationMenu'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import type { PageResponse } from '../../types/api'
import type { Notification } from '../../types/notifications'
import { resolveApiError } from '../../utils/apiErrors'

type ReadFilter = 'all' | 'unread'

const EMPTY_PAGE: PageResponse<Notification> = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [{ property: 'createdAt', direction: 'DESC' }],
}

export function NotificationsPage() {
  const navigate = useNavigate()
  const { refresh: refreshUnreadCount } = useNotifications()
  const [readFilter, setReadFilter] = useState<ReadFilter>('all')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [pageData, setPageData] = useState<PageResponse<Notification>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [markingAllRead, setMarkingAllRead] = useState(false)
  const [refreshToken, setRefreshToken] = useState(0)

  const readParam = useMemo(() => {
    if (readFilter === 'unread') {
      return false
    }
    return undefined
  }, [readFilter])

  useEffect(() => {
    let cancelled = false

    async function loadNotifications() {
      setLoading(true)
      setError(null)
      try {
        const response = await notificationsApi.list({
          page,
          size,
          sort: 'createdAt,desc',
          read: readParam,
        })
        if (!cancelled) {
          setPageData(response)
        }
      } catch (loadError) {
        if (!cancelled) {
          setError(resolveApiError(loadError, 'Unable to load notifications.'))
          setPageData(EMPTY_PAGE)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadNotifications()

    return () => {
      cancelled = true
    }
  }, [page, readParam, refreshToken, size])

  const handleNotificationClick = useCallback(
    async (notification: Notification) => {
      try {
        if (!notification.read) {
          await notificationsApi.markRead(notification.id)
          await refreshUnreadCount()
          setRefreshToken((current) => current + 1)
        }
      } catch {
        // Continue navigation even if mark-read fails.
      }

      if (notification.actionPath) {
        navigate(notification.actionPath)
      }
    },
    [navigate, refreshUnreadCount],
  )

  const handleMarkAllRead = useCallback(async () => {
    setMarkingAllRead(true)
    setError(null)
    try {
      await notificationsApi.markAllRead()
      await refreshUnreadCount()
      setRefreshToken((current) => current + 1)
    } catch (markError) {
      setError(resolveApiError(markError, 'Unable to mark notifications as read.'))
    } finally {
      setMarkingAllRead(false)
    }
  }, [refreshUnreadCount])

  return (
    <Box>
      <PageHeader
        description="Review platform updates about certificate reviews, account changes, and onboarding."
        title="Notifications"
      />

      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2, gap: 2, flexWrap: 'wrap' }}>
        <Tabs
          onChange={(_event, value: ReadFilter) => {
            setReadFilter(value)
            setPage(0)
          }}
          value={readFilter}
        >
          <Tab label="All" value="all" />
          <Tab label="Unread" value="unread" />
        </Tabs>
        <Button disabled={markingAllRead || loading} onClick={() => void handleMarkAllRead()} variant="outlined">
          Mark all as read
        </Button>
      </Box>

      {error ? (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      ) : null}

      <Paper variant="outlined">
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
            <CircularProgress />
          </Box>
        ) : pageData.content.length === 0 ? (
          <Box sx={{ px: 3, py: 6 }}>
            <Alert severity="info">{readFilter === 'unread' ? 'No unread notifications.' : 'No notifications yet.'}</Alert>
          </Box>
        ) : (
          <List disablePadding>
            {pageData.content.map((notification) => (
              <NotificationListItem key={notification.id} notification={notification} onNavigate={(item) => void handleNotificationClick(item)} />
            ))}
          </List>
        )}

        {!loading && pageData.totalElements > 0 ? (
          <TablePaginationBar
            onPageChange={setPage}
            onSizeChange={(nextSize) => {
              setSize(nextSize)
              setPage(0)
            }}
            page={pageData.page}
            size={pageData.size}
            totalElements={pageData.totalElements}
          />
        ) : null}
      </Paper>
    </Box>
  )
}
