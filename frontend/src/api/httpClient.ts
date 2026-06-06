import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'
const TOKEN_STORAGE_KEY = 'elh.accessToken'
export const AUTH_UNAUTHORIZED_EVENT = 'elh:auth:unauthorized'

export const tokenStorage = {
  get: () => window.sessionStorage.getItem(TOKEN_STORAGE_KEY),
  set: (token: string) => window.sessionStorage.setItem(TOKEN_STORAGE_KEY, token),
  clear: () => window.sessionStorage.removeItem(TOKEN_STORAGE_KEY),
}

export const httpClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    Accept: 'application/json',
  },
})

httpClient.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      tokenStorage.clear()
      window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT))
    }
    return Promise.reject(error)
  },
)
