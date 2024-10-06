<template>
  <el-form inline>
    <el-form-item label="Week:">
      <el-select
        v-model="selectedWeekRange"
        placeholder="Select Week"
        style="width: 240px"
        @change="emitSearchCriteria"
        popper-class="week-dropdown"
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

const selectedWeekNumber = defineModel()
const selectedWeekRange = ref<string>('') // E.g., '07-31-2023 -- 08-06-2023'
const settingsStore = useSettingsStore()
const allWeeks = ref<WeekInfo[]>([])

onMounted(async () => {
  if (!settingsStore.defaultSectionId) {
    return
  }
  const result: FindSectionByIdResponse = await findSectionById(settingsStore.defaultSectionId)
  const currentSection = result.data
  allWeeks.value = getAllWeeksInSection(currentSection)
  // By default, select the current week
  selectedWeekRange.value = getCurrentWeekRange()
  selectedWeekNumber.value = getCurrentWeekNumber()
})

// Emits
const emit = defineEmits(['search', 'reset'])

// Methods
const emitSearchCriteria = () => {
  const selectedWeekObj = allWeeks.value.find(
    (week) => `${week.monday} -- ${week.sunday}` === selectedWeekRange.value
  )
  if (selectedWeekObj) {
    selectedWeekNumber.value = selectedWeekObj.weekNumber // Capture the weekNumber
  }
  emit('search')
}

const emitResetCriteria = () => {
  selectedWeekRange.value = getCurrentWeekRange()
  selectedWeekNumber.value = getCurrentWeekNumber()
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
