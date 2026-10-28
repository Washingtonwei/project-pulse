import { defineStore } from 'pinia'
import { ref } from 'vue'

const TOKEN_KEY = 'token'

function readToken(storage: Storage): string {
  const storedToken = storage.getItem(TOKEN_KEY)
  if (!storedToken) return ''

  // Support a token written by the former Pinia persistence configuration.
  try {
    const persistedState = JSON.parse(storedToken) as { token?: unknown }
    return typeof persistedState.token === 'string' ? persistedState.token : storedToken
  } catch {
    return storedToken
  }
}

/*
  defineStore() takes three arguments:
  - the name of the store, must be unique
  - a function that returns the store's state and actions
  - an optional object with options
*/
export const useTokenStore = defineStore(
  'token',
  () => {
    // A session token takes precedence so a non-remembered login is confined to this tab.
    const token = ref<string>(readToken(sessionStorage) || readToken(localStorage))

    const setToken = (newToken: string, rememberMe: boolean) => {
      // A user may change their choice between logins; never leave a second active copy behind.
      sessionStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(TOKEN_KEY)

      token.value = newToken
      const storage = rememberMe ? localStorage : sessionStorage
      storage.setItem(TOKEN_KEY, newToken)
    }

    const removeToken = () => {
      token.value = ''
      sessionStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(TOKEN_KEY)
    }

    return {
      token,
      setToken,
      removeToken
    }
  }
)
