<template>
  <el-form inline>
    <el-form-item label="Start Week:">
      <div class="styled-week-input">
        <input
          type="week"
          v-model="selectedStartWeekNumber"
          :min="firstWeek.weekNumber"
          :max="lastWeek.weekNumber"
          :disabled="!firstWeek.weekNumber || !lastWeek.weekNumber"
          class="el-input__inner"
          ref="startWeekInput"
          @click="showNativeStartWeekPicker"
        />
      </div>
    </el-form-item>
    <el-form-item label="End Week:">
      <div class="styled-week-input">
        <input
          type="week"
          v-model="selectedEndWeekNumber"
          :min="selectedStartWeekNumber"
          :max="lastWeek.weekNumber"
          :disabled="!firstWeek.weekNumber || !lastWeek.weekNumber"
          class="el-input__inner"
          ref="endWeekInput"
          @click="showNativeEndWeekPicker"
        />
      </div>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="emitSearchCriteria">Query</el-button>
      <el-button @click="emitResetCriteria">Reset</el-button>
    </el-form-item>
  </el-form>
  <el-form inline>
    <el-form-item label="Start Week:">
      <div class="week-range">{{ selectedStartWeekDateRange }}</div>
    </el-form-item>
    <el-form-item label="End Week:">
      <div class="week-range">{{ selectedEndWeekDateRange }}</div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { findSectionById } from '@/apis/section'
import type { FindSectionByIdResponse, WeekInfo } from '@/apis/section/types'
import { getAllWeeksInSection, getCurrentWeekNumber } from '@/utils/week'

const selectedStartWeekNumber = defineModel<string>('selectedStartWeekNumber')
const selectedEndWeekNumber = defineModel<string>('selectedEndWeekNumber')

const selectedStartWeekDateRange = computed(() => {
  const selected = allWeeks.value.find((week) => week.weekNumber === selectedStartWeekNumber.value)
  return selected
    ? `${selected.monday} -- ${selected.sunday}`
    : 'The picked week is not in the current section.'
})

const selectedEndWeekDateRange = computed(() => {
  const selected = allWeeks.value.find((week) => week.weekNumber === selectedEndWeekNumber.value)
  return selected
    ? `${selected.monday} -- ${selected.sunday}`
    : 'The picked week is not in the current section.'
})

const settingsStore = useSettingsStore()
const allWeeks = ref<WeekInfo[]>([])

const firstWeek = ref<WeekInfo>({ weekNumber: '', monday: '', sunday: '', isActive: false }) // This will hold the first week in the current section
const lastWeek = ref<WeekInfo>({ weekNumber: '', monday: '', sunday: '', isActive: false }) // This will hold the last week in the current section

onMounted(async () => {
  if (!settingsStore.defaultSectionId) {
    return
  }
  const result: FindSectionByIdResponse = await findSectionById(settingsStore.defaultSectionId)
  const currentSection = result.data
  if (!currentSection) {
    return
  }
  allWeeks.value = getAllWeeksInSection(currentSection)
  if (allWeeks.value.length > 0) {
    // allWeeks is sorted by weekNumber, so the first and last weeks are the first and last elements
    firstWeek.value = allWeeks.value[0]
    lastWeek.value = allWeeks.value[allWeeks.value.length - 1]
  }

  // By default, select start and end to the current week
  selectedStartWeekNumber.value = getCurrentWeekNumber() // Default to the current week number
  selectedEndWeekNumber.value = getCurrentWeekNumber() // Default to the current week number
})

// Emits
const emit = defineEmits(['search', 'reset'])

// Methods
const emitSearchCriteria = () => {
  emit('search')
}

const emitResetCriteria = () => {
  selectedStartWeekNumber.value = getCurrentWeekNumber()
  selectedEndWeekNumber.value = getCurrentWeekNumber()
  emit('reset')
}

const startWeekInput = ref<HTMLInputElement | null>(null) // Reference to the native week input element
const endWeekInput = ref<HTMLInputElement | null>(null) // Reference to the native week input element

const showNativeStartWeekPicker = () => {
  if (startWeekInput.value && typeof startWeekInput.value.showPicker === 'function') {
    startWeekInput.value.showPicker()
  }
}

const showNativeEndWeekPicker = () => {
  if (endWeekInput.value && typeof endWeekInput.value.showPicker === 'function') {
    endWeekInput.value.showPicker()
  }
}
</script>

<style scoped lang="scss">
.styled-week-input {
  display: inline-block;
  width: 160px;
  position: relative;
  font-size: 14px;
  line-height: 30px;
}

input[type='week'].el-input__inner {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  padding: 0 11px;
  height: 30px;
  line-height: 30px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  outline: none;

  &:focus {
    border-color: #409eff;
  }
}
.week-range {
  display: inline-block;
  width: 160px;
  position: relative;
  font-size: 14px;
  line-height: 30px;
  font-weight: bold;
}
</style>
