import { defineStore } from 'pinia'
import { ref } from 'vue'

/*
  defineStore() takes three arguments:
  - the name of the store, must be unique
  - a function that returns the store's state and actions
  - an optional object with options
*/
export const useTokenStore = defineStore(
  'token',
  () => {
    const token = ref<string>('')

    const setToken = (newToken: string) => {
      token.value = newToken
    }

    const removeToken = () => {
      token.value = ''
    }

    return {
      token,
      setToken,
      removeToken
    }
  },
  { persist: true }
)
