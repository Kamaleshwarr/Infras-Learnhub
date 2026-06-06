import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/authApi'
import { tokenStorage } from '../api/httpClient'
import type { LoginResponse, UserProfile, UserRole } from '../types/auth'

interface AuthContextValue {
  user: UserProfile | null
  isAuthenticated: boolean
  isLoading: boolean
  hasRole: (role: UserRole) => boolean
  login: (email: string, password: string) => Promise<LoginResponse>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [isLoading, setIsLoading] = useState(true)

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

  const hasRole = useCallback(
    (role: UserRole) => Boolean(user?.roles.includes(role)),
    [user],
  )

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isLoading,
      hasRole,
      login,
      logout,
    }),
    [user, isLoading, hasRole, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
