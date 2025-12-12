import type { Instructor } from '@/apis/instructor/types'
import type { Student } from '@/apis/student/types'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserInfoStore = defineStore(
  'userInfo',
  () => {
    const userInfo = ref<Student | Instructor | null>(null)

    const setUserInfo = (newUserInfo: Student | Instructor) => {
      userInfo.value = newUserInfo
    }

    const removeUserInfo = () => {
      userInfo.value = null
    }

    const roleList = computed<string[]>(() => {
      if (!userInfo.value) return [] // pre-login / logging out

      const raw = (userInfo.value as any).roles as string // e.g. "admin instructor"
      return raw
        .split(/\s+/)
        .map((r) => r.toLowerCase())
        .filter(Boolean)
    })

    const hasRole = (role: string) => {
      return roleList.value.includes(role.toLowerCase())
    }

    const isAuthenticated = computed(() => userInfo.value !== null)

    const isAdmin = computed(() => hasRole('admin'))
    const isInstructor = computed(() => hasRole('instructor'))
    const isStudent = computed(() => hasRole('student'))

    return {
      userInfo,
      setUserInfo,
      removeUserInfo,
      isAuthenticated,
      roleList,
      hasRole,
      isAdmin,
      isInstructor,
      isStudent
    }
  },
  { persist: true }
)
