<template>
  <el-form inline>
    <el-form-item label="Start Week:">
      <el-select
        v-model="selectedStartWeekRange"
        placeholder="Select Week"
        style="width: 240px"
        popper-class="week-dropdown"
        @change="handleStartWeekChange"
      >
        <el-option
          v-for="week in allWeeks"
          :key="week.weekNumber"
          :label="week.monday + ' -- ' + week.sunday"
          :value="week.monday + ' -- ' + week.sunday"
        >
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="End Week:">
      <el-select
        v-model="selectedEndWeekRange"
        placeholder="Select Week"
        style="width: 240px"
        popper-class="week-dropdown"
        @change="handleEndWeekChange"
      >
        <el-option
          v-for="week in allWeeks"
          :key="week.weekNumber"
          :label="week.monday + ' -- ' + week.sunday"
          :value="week.monday + ' -- ' + week.sunday"
        >
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="emitSearchCriteria">Query</el-button>
      <el-button @click="emitResetCriteria">Reset</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { findSectionById } from '@/apis/section'
import type { FindSectionByIdResponse, WeekInfo } from '@/apis/section/types'
import { getAllWeeksInSection, getCurrentWeekNumber, getCurrentWeekRange } from '@/utils/week'

const selectedStartWeekNumber = defineModel('selectedStartWeekNumber')
const selectedEndWeekNumber = defineModel('selectedEndWeekNumber')
const selectedStartWeekRange = ref<string>('') // E.g., '07-31-2023 -- 08-06-2023'
const selectedEndWeekRange = ref<string>('')
const settingsStore = useSettingsStore()
const allWeeks = ref<WeekInfo[]>([])

onMounted(async () => {
  if (!settingsStore.defaultSectionId) {
    return
  }
  const result: FindSectionByIdResponse = await findSectionById(settingsStore.defaultSectionId)
  const currentSection = result.data
  allWeeks.value = getAllWeeksInSection(currentSection)
  // By default, select start and end to the current week
  selectedStartWeekRange.value = getCurrentWeekRange()
  selectedEndWeekRange.value = getCurrentWeekRange()
  selectedStartWeekNumber.value = getCurrentWeekNumber()
  selectedEndWeekNumber.value = getCurrentWeekNumber()
})

function handleStartWeekChange() {
  const selectedStartWeekObj = allWeeks.value.find(
    (week) => `${week.monday} -- ${week.sunday}` === selectedStartWeekRange.value
  )
  if (selectedStartWeekObj) {
    selectedStartWeekNumber.value = selectedStartWeekObj.weekNumber // Capture the weekNumber
  }
}

function handleEndWeekChange() {
  const selectedEndWeekObj = allWeeks.value.find(
    (week) => `${week.monday} -- ${week.sunday}` === selectedEndWeekRange.value
  )
  if (selectedEndWeekObj) {
    selectedEndWeekNumber.value = selectedEndWeekObj.weekNumber // Capture the weekNumber
  }
}

// Emits
const emit = defineEmits(['search', 'reset'])

// Methods
const emitSearchCriteria = () => {
  emit('search')
}

const emitResetCriteria = () => {
  selectedStartWeekRange.value = getCurrentWeekRange()
  selectedEndWeekRange.value = getCurrentWeekRange()
  selectedStartWeekNumber.value = getCurrentWeekNumber()
  selectedEndWeekNumber.value = getCurrentWeekNumber()
  emit('reset')
}
</script>

<style scoped>
.week-dropdown .el-select-dropdown__item {
  display: inline-block;
  width: 200px; /* Set a fixed width for each column */
  box-sizing: border-box;
  padding: 10px; /* Adjust padding as necessary */
  text-align: center; /* Align the text centrally if needed */
}

.week-dropdown .el-select-dropdown__list {
  display: flex;
  flex-wrap: wrap;
}
</style>
