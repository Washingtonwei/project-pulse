import type { Instructor } from '@/apis/instructor/types'
import type { Student } from '@/apis/student/types'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserInfoStore = defineStore(
  'userInfo',
  () => {
    const userInfo = ref<Student | Instructor>()
    const setUserInfo = (newUserInfo: Student | Instructor) => {
      userInfo.value = newUserInfo
    }
    const removeUserInfo = () => {
      userInfo.value = undefined
    }
    return { userInfo, setUserInfo, removeUserInfo }
  },
  { persist: true }
)
