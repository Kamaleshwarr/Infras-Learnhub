import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { notificationsApi } from '../api/notificationsApi'

const POLL_INTERVAL_MS = 60_000

export interface NotificationContextValue {
  unreadCount: number
  loading: boolean
  error: string | null
  refresh: () => Promise<void>
}

export const NotificationContext = createContext<NotificationContextValue | undefined>(undefined)

interface NotificationProviderProps {
  children: ReactNode
}

export function NotificationProvider({ children }: NotificationProviderProps) {
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

  const value = useMemo(
    () => ({
      unreadCount,
      loading,
      error,
      refresh,
    }),
    [error, loading, refresh, unreadCount],
  )

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>
}
