import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSettingsStore = defineStore(
  'settings',
  () => {
    const defaultSectionId = ref<number>(NaN)
    const setDefaultSectionId = (newDefaultSectionId: number) => {
      defaultSectionId.value = newDefaultSectionId
    }
    const removeDefaultSectionId = () => {
      defaultSectionId.value = NaN
    }

    const defaultCourseId = ref<number>(NaN)

    // Plain setter – use this for login/backend restore
    const setDefaultCourseId = (courseId: number) => {
      defaultCourseId.value = courseId
    }

    // UI-only setter – use this when user changes course in the app
    const setDefaultCourseIdAndResetSection = (newDefaultCourseId: number) => {
      defaultCourseId.value = newDefaultCourseId
      // 🔴 IMPORTANT: when course changes, clear the section
      removeDefaultSectionId()
    }
    const removeDefaultCourseId = () => {
      defaultCourseId.value = NaN
    }

    return {
      defaultSectionId,
      setDefaultSectionId,
      removeDefaultSectionId,
      defaultCourseId,
      setDefaultCourseId,
      setDefaultCourseIdAndResetSection,
      removeDefaultCourseId
    }
  },
  { persist: true }
)
