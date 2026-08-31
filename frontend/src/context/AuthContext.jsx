import { createContext, useContext, useEffect, useState } from 'react'
import { authService } from '../services/authService.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let active = true

    const restoreSession = async () => {
      authService.clearObsoleteAuthState()

      if (!authService.hasToken()) {
        if (active) setIsLoading(false)
        return
      }

      try {
        const currentUser = await authService.getCurrentUser()
        if (active) setUser(currentUser)
      } catch {
        authService.logout()
      } finally {
        if (active) setIsLoading(false)
      }
    }

    restoreSession()
    return () => {
      active = false
    }
  }, [])

  const login = (userData, token) => {
    authService.storeToken(token)
    setUser(userData)
  }

  const logout = () => {
    authService.logout()
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, isLoading, login, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
