import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'
const TOKEN_STORAGE_KEY = 'elh.accessToken'

export const tokenStorage = {
  get: () => window.localStorage.getItem(TOKEN_STORAGE_KEY),
  set: (token: string) => window.localStorage.setItem(TOKEN_STORAGE_KEY, token),
  clear: () => window.localStorage.removeItem(TOKEN_STORAGE_KEY),
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
