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
    const setDefaultCourseId = (newDefaultCourseId: number) => {
      defaultCourseId.value = newDefaultCourseId
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
      removeDefaultCourseId
    }
  },
  { persist: true }
)
