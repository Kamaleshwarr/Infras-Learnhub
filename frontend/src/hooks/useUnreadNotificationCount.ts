import { useCallback, useEffect, useState } from 'react'
import { notificationsApi } from '../api/notificationsApi'

const POLL_INTERVAL_MS = 60_000

export function useUnreadNotificationCount() {
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(async () => {
    try {
      const response = await notificationsApi.unreadCount()
      setUnreadCount(response.count)
      setError(null)
    } catch {
      setError('Unable to load notification count.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()

    const intervalId = window.setInterval(() => {
      void refresh()
    }, POLL_INTERVAL_MS)

    const handleFocus = () => {
      void refresh()
    }

    window.addEventListener('focus', handleFocus)

    return () => {
      window.clearInterval(intervalId)
      window.removeEventListener('focus', handleFocus)
    }
  }, [refresh])

  const decrementUnreadCount = useCallback((amount = 1) => {
    setUnreadCount((current) => Math.max(0, current - amount))
  }, [])

  const clearUnreadCount = useCallback(() => {
    setUnreadCount(0)
  }, [])

  return {
    unreadCount,
    loading,
    error,
    refresh,
    decrementUnreadCount,
    clearUnreadCount,
  }
}
