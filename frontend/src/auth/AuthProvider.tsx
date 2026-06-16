import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/authApi'
import { AUTH_UNAUTHORIZED_EVENT, tokenStorage } from '../api/httpClient'
import { getPrimaryRole } from '../types/auth'
import type { LoginResponse, UserProfile, UserRole } from '../types/auth'

interface AuthContextValue {
  user: UserProfile | null
  currentRole: UserRole | null
  isAuthenticated: boolean
  isLoading: boolean
  isAdmin: boolean
  isEmployee: boolean
  hasRole: (role: UserRole) => boolean
  login: (email: string, password: string) => Promise<LoginResponse>
  logout: () => void
  refreshProfile: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    function handleUnauthorized() {
      setUser(null)
    }

    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
    return () => window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
  }, [])

  useEffect(() => {
    let mounted = true
    async function loadProfile() {
      if (!tokenStorage.get()) {
        setIsLoading(false)
        return
      }
      try {
        const profile = await authApi.me()
        if (mounted) {
          setUser(profile)
        }
      } catch {
        tokenStorage.clear()
      } finally {
        if (mounted) {
          setIsLoading(false)
        }
      }
    }

    loadProfile()
    return () => {
      mounted = false
    }
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login({ email, password })
    tokenStorage.set(response.accessToken)
    setUser(response.user)
    return response
  }, [])

  const logout = useCallback(() => {
    tokenStorage.clear()
    setUser(null)
  }, [])

  const refreshProfile = useCallback(async () => {
    if (!tokenStorage.get()) {
      setUser(null)
      return
    }
    const profile = await authApi.me()
    setUser(profile)
  }, [])

  const hasRole = useCallback(
    (role: UserRole) => Boolean(user?.roles.includes(role)),
    [user],
  )
  const currentRole = getPrimaryRole(user)

  const value = useMemo(
    () => ({
      user,
      currentRole,
      isAuthenticated: Boolean(user),
      isLoading,
      isAdmin: hasRole('ADMIN'),
      isEmployee: hasRole('EMPLOYEE'),
      hasRole,
      login,
      logout,
      refreshProfile,
    }),
    [user, currentRole, isLoading, hasRole, login, logout, refreshProfile],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
