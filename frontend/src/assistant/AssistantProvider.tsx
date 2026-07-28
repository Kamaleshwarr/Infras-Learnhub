import { createContext, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import { assistantApi } from '../api/assistantApi'

export interface AssistantContextValue {
  enabled: boolean
  loading: boolean
  error: string | null
  refreshStatus: () => Promise<void>
}

export const AssistantContext = createContext<AssistantContextValue | undefined>(undefined)

interface AssistantProviderProps {
  children: ReactNode
}

export function AssistantProvider({ children }: AssistantProviderProps) {
  const [enabled, setEnabled] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const mountedRef = useRef(true)

  const refreshStatus = useCallback(async () => {
    try {
      const status = await assistantApi.getStatus()
      if (!mountedRef.current) {
        return
      }
      setEnabled(status.enabled)
      setError(null)
    } catch {
      if (!mountedRef.current) {
        return
      }
      setEnabled(false)
      setError('Unable to load assistant status.')
    } finally {
      if (mountedRef.current) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    mountedRef.current = true
    void refreshStatus()

    return () => {
      mountedRef.current = false
    }
  }, [refreshStatus])

  const value = useMemo(
    () => ({
      enabled,
      loading,
      error,
      refreshStatus,
    }),
    [enabled, error, loading, refreshStatus],
  )

  return <AssistantContext.Provider value={value}>{children}</AssistantContext.Provider>
}
