import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

let isRefreshing = false

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken && !isRefreshing) {
        originalRequest._retry = true
        isRefreshing = true
        try {
          const { data } = await api.post('/auth/refresh', { refreshToken })
          localStorage.setItem('refreshToken', data.refreshToken)
          originalRequest.headers['Authorization'] = `Bearer ${data.token}`
          isRefreshing = false
          return api(originalRequest)
        } catch {
          isRefreshing = false
          localStorage.removeItem('refreshToken')
        }
      }
      const auth = useAuthStore()
      auth.logout()
      router.push('/')
    }
    return Promise.reject(error)
  },
)

export default api
