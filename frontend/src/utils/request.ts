import axios from 'axios'
import type { AxiosError, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import router from '@/router'

const devBaseURL = import.meta.env.VITE_SERVER_URL as string | undefined
// Use the dev server URL in development, and a relative path in production and staging. This allows the frontend to be served from the same domain as the backend in production and staging, while still allowing local development against a separate backend server.
const baseURL = import.meta.env.DEV ? devBaseURL : '/api/v1'
const requestInstance = axios.create({ baseURL, timeout: 10_000 }) // 10 seconds

declare module 'axios' {
  interface AxiosRequestConfig {
    // Set on a call whose caller handles a 404 itself: the response interceptor then neither toasts nor routes to the
    // not-found page, which would leave the page and lose unsaved work. For example, the document section editor's
    // Remove passes it, and on a 404 (a teammate already deleted the item) drops the item from its list instead.
    // Every other status is handled as usual.
    handleNotFound?: boolean
  }
}

// Simple toast cooldown (prevents spam on cascading failures)
let lastToastAt = 0

/**
 * Shows an error toast message with a short cooldown to prevent
 * multiple identical toasts during cascading failures.
 *
 * @param msg - The message to display.
 * @param cooldownMs - Minimum milliseconds between two toasts.
 */
function toastOnce(msg: string, cooldownMs = 1500) {
  const now = Date.now()
  if (now - lastToastAt > cooldownMs) {
    ElMessage.error(msg)
    lastToastAt = now
  }
}

/**
 * Extracts a human-readable error message from any thrown error.
 *
 * This helper normalizes different Axios and JavaScript error shapes
 * into a single string message for consistent user feedback.
 *
 * Typical sources:
 *  - API response errors (e.g. { message: 'Invalid token' })
 *  - Network errors (e.g. "Network Error", "timeout exceeded")
 *  - Custom thrown Error objects
 *
 * @param err - The unknown error object thrown from an Axios request or other code.
 * @returns A user-friendly error message string.
 */
function extractErrorMessage(err: unknown): string {
  // Case 1: Axios-generated error (includes response, config, code, etc.)
  if (axios.isAxiosError(err)) {
    // We narrow the type so TypeScript knows ax.response?.data could include message, error, or code.
    // This helps us safely access them without compiler warnings.
    const ax = err as AxiosError<{ message?: string; error?: string; code?: string }>

    return ax.response?.data?.message || ax.response?.data?.error || ax.message || 'Request failed'
  }

  // Case 2: Standard JS Error object
  if (err instanceof Error) return err.message

  // Case 3: Unknown error type — fail-safe fallback
  return 'Request failed'
}

// Centralized logout
function hardLogout() {
  const tokenStore = useTokenStore()
  const userInfoStore = useUserInfoStore()
  tokenStore.removeToken()
  userInfoStore.removeUserInfo()
}

// This function checks if the current route matches the one you're about to navigate to.
// If you're already there, it skips calling router.push.
function isOnRoute(name: string): boolean {
  const r = router.currentRoute?.value
  return r?.name === name
}

// Configure request interceptor
requestInstance.interceptors.request.use(
  (config) => {
    // Do something before request is sent
    const tokenStore = useTokenStore()
    if (tokenStore.token) {
      config.headers = config.headers ?? {} // Ensure headers object exists
      ;(config.headers as any).Authorization = `Bearer ${tokenStore.token}`
    }

    return config
  },
  (error) => {
    // Do something with request error
    return Promise.reject(error)
  }
)

// Configure response interceptor
requestInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    // Any status code that falls within the range of 2xx causes this function to trigger
    return response.data
  },
  (error) => {
    // Network or unexpected (non-Axios) error
    if (!axios.isAxiosError(error) || !error.response) {
      toastOnce(extractErrorMessage(error))
      return Promise.reject(error)
    }

    const { status, data } = error.response
    const msg = (data && (data.message || data.error)) || `HTTP ${status}: Something went wrong`

    // The caller asked to handle a 404 itself (see handleNotFound above): no toast, no routing.
    //
    // How the flag gets here: error.config is not part of the server's answer. It is the config this request was sent
    // with, which Axios builds in the browser (url, method, params, headers, plus anything extra the API function
    // passed, such as handleNotFound) and attaches to every error, so a handler can tell which request failed. The
    // server's answer is error.response. Axios builds the HTTP request only from the fields it knows, so handleNotFound
    // never reaches the server; it is a note this request carries for this interceptor. For example:
    //   request.delete(url, { params: { version: 0 }, handleNotFound: true }) and the server answers 404
    //   error.config   -> { url, method: 'delete', params: { version: 0 }, handleNotFound: true, ... }  (what we sent)
    //   error.response -> { status: 404, data: { flag: false, code: 404, message: '...' } }              (what came back)
    // A call that does not pass the flag has error.config.handleNotFound undefined and takes the usual path below.
    // error.config can be missing on some errors, hence the ?. (the check is then false).
    if (status === 404 && error.config?.handleNotFound) {
      return Promise.reject(error)
    }

    // Avoid toasting on 401 (we redirect instead)
    if (status !== 401) toastOnce(msg)

    switch (status) {
      case 401: {
        // Token missing/expired → clear state and go to login
        hardLogout()
        if (!isOnRoute('login')) {
          router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
        }
        break
      }
      case 403:
        if (!isOnRoute('forbidden')) router.push({ name: 'forbidden' })
        break
      case 404:
        if (data?.message === 'Could not find section with this property: default section :(') {
          if (!isOnRoute('sections')) router.push({ name: 'sections' })
        } else if (!isOnRoute('not-found')) {
          router.push({ name: 'not-found' })
        }
        break
      case 408:
      case 504: {
        toastOnce('The request timed out. Please try again.')
        break
      }
      case 500:
        // Optionally route to a server error page
        // if (!isOnRoute('server-error')) router.push({ name: 'server-error' })
        break
    }
    return Promise.reject(error)
  }
)

export default requestInstance
