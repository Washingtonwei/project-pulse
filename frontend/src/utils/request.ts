import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import router from '@/router'

const baseURL = import.meta.env.VITE_SERVER_URL as string
const requestInstance = axios.create({ baseURL, timeout: 5000 })

// Config request interceptor
requestInstance.interceptors.request.use(
  (config) => {
    // Do something before request is sent
    const tokenStore = useTokenStore()
    if (tokenStore.token) {
      // If token exists, set in header.
      config.headers.Authorization = `Bearer ${tokenStore.token}`
    }
    return config
  },
  (error) => {
    // Do something with request error
    return Promise.reject(error)
  }
)

// Config response interceptor
requestInstance.interceptors.response.use(
  (response) => {
    // Any status code that falls within the range of 2xx causes this function to trigger
    return response.data
  },
  (error) => {
    if (!error.response) {
      ElMessage.error(`${error.message}`)
      return Promise.reject(error)
    }
    // Any status code that falls outside the range of 2xx causes this function to trigger
    ElMessage.error(error.response.data.message || 'Something went wrong!')

    switch (error.response.status) {
      case 401: {
        // There are two cases for 401 error:
        // Case 1. Invalid token or token expired
        // Case 2. There is no token at all
        const tokenStore = useTokenStore()
        const userInfoStore = useUserInfoStore()
        tokenStore.removeToken() // Remove token from store if it exists, this is for case 1
        userInfoStore.removeUserInfo() // Reset user info, this is for case 1
        router.push({ name: 'login' })
        break
      }
      case 403:
        router.push({ name: 'forbidden' })
        break
      case 404:
        if (
          error.response.data.message ===
          'Could not find section with this property: default section :('
        ) {
          router.push({ name: 'sections' })
        } else {
          router.push({ name: 'not-found' })
        }
        break
      case 500:
        // router.push({ path: '/500' })
        break
    }
    return Promise.reject(error)
  }
)

export default requestInstance
