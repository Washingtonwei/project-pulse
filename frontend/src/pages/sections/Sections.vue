<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Section Management</span>
        <div class="extra">
          <el-button type="primary" @click="showAddDialog()" icon="Plus">
            Add new section
          </el-button>
        </div>
      </div>
    </template>
    <!-- Search Form -->
    <el-form inline>
      <el-form-item label="Section Name:">
        <el-input v-model="searchCriteria.sectionName" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadSections">Query</el-button>
        <el-button @click="resetSearchCriteria">Reset</el-button>
      </el-form-item>
    </el-form>
    <!-- Section Table -->
    <el-table :data="sections" style="width: 100%" stripe border v-loading="loading">
      <el-table-column label="Default Section" width="150px">
        <template #default="{ row }">
          <el-radio
            v-model="defaultSectionId"
            :value="row.sectionId"
            @change="updateDefaultSection(row.sectionId)"
          ></el-radio>
        </template>
      </el-table-column>
      <el-table-column label="Id" prop="sectionId"></el-table-column>
      <el-table-column label="Name" prop="sectionName"> </el-table-column>
      <el-table-column label="Start Date" prop="startDate"></el-table-column>
      <el-table-column label="End Date" prop="endDate"></el-table-column>
      <el-table-column label="Rubric">
        <template #default="{ row }">
          <el-select
            v-model="row.rubricId"
            placeholder="Select a rubric"
            style="width: 100%"
            @change="assignRubricToSection(row.sectionId, row.rubricId)"
          >
            <el-option
              v-for="rubric in rubrics"
              :key="rubric.rubricId"
              :label="rubric.rubricName"
              :value="rubric.rubricId"
            ></el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="Operations" width="150">
        <template #default="{ row }">
          <el-button
            icon="Edit"
            circle
            plain
            type="primary"
            @click="showEditDialog(row)"
          ></el-button>
          <el-button
            icon="Calendar"
            circle
            plain
            type="primary"
            @click="showActiveWeeksDialog(row)"
          ></el-button>
          <el-button
            icon="Promotion"
            circle
            plain
            type="primary"
            @click="showInviteStudentsDialog(row)"
          ></el-button>
          <!-- <el-button
            icon="Delete"
            circle
            plain
            type="danger"
            @click="deleteExistingSection(row)"
          ></el-button> -->
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="No data is available." />
      </template>
    </el-table>
    <!-- Pagination -->
    <el-pagination
      v-model:current-page="pageNumber"
      v-model:page-size="pageSize"
      :page-sizes="[2, 5, 10]"
      layout="jumper, total, sizes, prev, pager, next"
      background
      :total="totalElements"
      @size-change="handlePageSizeChange"
      @current-change="handlePageNumberChange"
      style="margin-top: 20px; justify-content: flex-end"
    />
    <!-- Dialog for adding/editing section -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form
        ref="sectionForm"
        :model="sectionData"
        :rules="rules"
        label-width="110px"
        style="padding-right: 30px"
        label-position="left"
      >
        <el-form-item label="Id:" v-if="dialogTitle == 'Edit a section'">
          <el-input v-model="sectionData.sectionId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name:" prop="name">
          <el-input v-model="sectionData.sectionName" minlength="1"></el-input>
        </el-form-item>
        <el-form-item label="Start Date:" prop="startDate">
          <el-date-picker
            v-model="sectionData.startDate"
            type="date"
            placeholder="Select a date"
            format="MM-DD-YYYY"
            value-format="MM-DD-YYYY"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="End Date:" prop="endDate">
          <el-date-picker
            v-model="sectionData.endDate"
            type="date"
            placeholder="Select a date"
            format="MM-DD-YYYY"
            value-format="MM-DD-YYYY"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false"> Cancel </el-button>
          <el-button
            type="primary"
            @click="dialogTitle == 'Add a section' ? addSection() : updateExistingSection()"
            :loading="buttonLoading"
          >
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
    <!-- Dialog for selecting active weeks in a section -->
    <el-dialog v-model="activeWeeksDialogVisible" title="Select Active Weeks" width="40%">
      <el-table :data="activeWeeks" style="width: 100%">
        <el-table-column label="Active" width="100">
          <template v-slot="scope">
            <el-checkbox v-model="scope.row.isActive"></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column label="Week Number" prop="weekNumber" width="150">
          <template #default="{ row }">
            {{ row.weekNumber }}
          </template>
        </el-table-column>
        <el-table-column label="Monday Date" prop="monday" width="150">
          <template #default="{ row }">
            {{ row.monday }}
          </template>
        </el-table-column>
        <el-table-column label="Sunday Date" prop="sunday" width="150">
          <template #default="{ row }">
            {{ row.sunday }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="activeWeeksDialogVisible = false"> Cancel </el-button>
          <el-button type="primary" @click="updateActiveWeeks()"> Confirm </el-button>
        </span>
      </template>
    </el-dialog>
    <!-- Dialog for inviting students to join a section -->
    <el-dialog
      title="Invite Users"
      v-model="inviteStudentDialogVisible"
      width="50%"
      destroy-on-close
    >
      <InviteUsersForm
        :sectionId="inviteStudentSectionId"
        :courseId="inviteStudentCourseId"
        @close-dialog="closeInviteUsersDialog"
      ></InviteUsersForm>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  searchSections,
  createSection,
  updateSection,
  assignRubricToSection,
  setUpActiveWeeks
} from '@/apis/section'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import type {
  SearchSectionByCriteriaResponse,
  Section,
  SectionSearchCriteria,
  WeekInfo
} from '@/apis/section/types'
import type { Rubric, SearchRubricByCriteriaResponse } from '@/apis/rubric/types'
import { searchRubrics } from '@/apis/rubric'
import moment from 'moment'
import { useSettingsStore } from '@/stores/settings'
import InviteUsersForm from './InviteUsersForm.vue'
import { setDefaultSection } from '@/apis/instructor'

const searchCriteria = ref<SectionSearchCriteria>({
  sectionName: ''
})

const sections = ref<Section[]>([])
const rubrics = ref<Rubric[]>([])

const settingsStore = useSettingsStore()
const defaultSectionId = ref<number>(settingsStore.defaultSectionId)
const loading = ref<boolean>(true) // Loading status
const buttonLoading = ref<boolean>(false) // Loading status for the buttons

// Pagination information
const pageNumber = ref<number>(1) // current page number, starting from 1
const pageSize = ref<number>(10) // number of elements per page
const totalElements = ref<number>(10) // total number of elements

const sectionForm = ref<FormInstance>()

// Load data when the component is mounted
onMounted(() => {
  loadRubrics()
  loadSections()
})

// Load rubrics
async function loadRubrics() {
  const result: SearchRubricByCriteriaResponse = await searchRubrics({})
  rubrics.value = result.data.content
}

// Load activities based on the search criteria
async function loadSections() {
  loading.value = true
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: SearchSectionByCriteriaResponse = await searchSections(
    { page: pageNumber.value - 1, size: pageSize.value },
    searchCriteria.value
  )
  sections.value = result.data.content

  // Update pagination information
  pageNumber.value = result.data.number + 1 // The page number starts from 0 on the back end, so add 1 here
  pageSize.value = result.data.size
  totalElements.value = result.data.totalElements
  loading.value = false
}

function resetSearchCriteria() {
  searchCriteria.value = {
    sectionName: ''
  }
  loadSections()
}

function handlePageNumberChange(newPageNumer: number) {
  pageNumber.value = newPageNumer // Not necessary since pageNumber already has a two-way binding
  loadSections()
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize // Not necessary since pageSize already has a two-way binding
  pageNumber.value = 1 // Reset the page number to 1 when the page size changes
  loadSections()
}

// Control the visibility of the dialog
const dialogVisible = ref(false)

const sectionData = ref<Section>({
  // Data of the section being added or edited
  sectionId: NaN,
  sectionName: '',
  startDate: '',
  endDate: ''
})

// Validation rules
const rules = {
  sectionName: [
    { required: true, message: 'Please provide the name of the section.', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: 'Please provide the start date of the section.', trigger: 'blur' }
  ],
  endDate: [
    { required: true, message: 'Please provide the end date of the section.', trigger: 'blur' }
  ]
}

function clearForm() {
  sectionData.value = {
    sectionId: NaN,
    sectionName: '',
    startDate: '',
    endDate: ''
  }
}

const dialogTitle = ref<string>('')

function showAddDialog() {
  clearForm()
  sectionForm.value?.clearValidate() // Clear the validation status of the form. The first time the dialog is opened, the form is not defined, so we need to check if it is defined before calling clearValidate()
  dialogTitle.value = 'Add a section'
  dialogVisible.value = true
}

async function addSection() {
  await sectionForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const newSection: Section = {
    sectionName: sectionData.value.sectionName,
    startDate: sectionData.value.startDate,
    endDate: sectionData.value.endDate
  }

  await createSection(newSection)
  ElMessage.success('Section added successfully')
  buttonLoading.value = false

  // Close the dialog
  dialogVisible.value = false

  // Reload the section table
  loadSections()
}

function showEditDialog(existingSection: Section) {
  clearForm()
  sectionForm.value?.clearValidate()
  dialogVisible.value = true
  dialogTitle.value = 'Edit a section'

  sectionData.value.sectionId = existingSection.sectionId as number
  sectionData.value.sectionName = existingSection.sectionName
  sectionData.value.startDate = existingSection.startDate
  sectionData.value.endDate = existingSection.endDate
}

async function updateExistingSection() {
  await sectionForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const updatedSection: Section = {
    sectionId: sectionData.value.sectionId as number,
    sectionName: sectionData.value.sectionName,
    startDate: sectionData.value.startDate,
    endDate: sectionData.value.endDate
  }

  // Call the API to update the section
  await updateSection(updatedSection)
  ElMessage.success('Section updated successfully')
  buttonLoading.value = false

  // Close the dialog
  dialogVisible.value = false

  // Reload the section table
  loadSections()
}

const activeWeeksDialogVisible = ref(false)
const activeWeeks = ref<WeekInfo[]>([])
const activeWeeksSectionId = ref<number>(NaN) // The section ID for which the active weeks are being set

function showActiveWeeksDialog(section: Section) {
  activeWeeksDialogVisible.value = true
  activeWeeksSectionId.value = section.sectionId as number
  activeWeeks.value = prepareActiveWeeks(section)
}

function prepareActiveWeeks(section: Section): WeekInfo[] {
  const allWeeks: WeekInfo[] = []
  const startDate = moment(section.startDate, 'MM-DD-YYYY')
  const endDate = moment(section.endDate, 'MM-DD-YYYY')

  if (!startDate.isValid() || !endDate.isValid()) {
    throw new Error('Invalid date format. Please use "MM-DD-YYYY".')
  }

  // if (startDate.day() !== 1 || endDate.day() !== 1) {
  //   throw new Error('Start and end dates must be Mondays.')
  // }

  if (startDate.isAfter(endDate)) {
    throw new Error('Start date must be before or equal to end date.')
  }

  let currentMonday = startDate.clone()

  while (currentMonday.isSameOrBefore(endDate)) {
    const currentSunday = currentMonday.clone().endOf('week').isoWeekday(7)

    const weekNumber = currentMonday.format('YYYY-[W]WW')

    allWeeks.push({
      weekNumber,
      monday: currentMonday.format('MM-DD-YYYY'),
      sunday: currentSunday.format('MM-DD-YYYY'),
      isActive: section.activeWeeks?.length != 0 ? section.activeWeeks!.includes(weekNumber) : true // If the section has active weeks, set the active status based on the active weeks; otherwise, set all weeks as active
    })

    currentMonday.add(1, 'week')
  }

  return allWeeks
}

async function updateActiveWeeks() {
  const activeWeekNumbers = activeWeeks.value
    .filter((week) => week.isActive)
    .map((week) => week.weekNumber)

  // Call the API to set up the active weeks
  await setUpActiveWeeks(activeWeeksSectionId.value, activeWeekNumbers)
  ElMessage.success('Active weeks updated successfully')

  // Close the dialog
  activeWeeksDialogVisible.value = false
  activeWeeksSectionId.value = NaN

  // Reload the section table
  loadSections()
}

async function updateDefaultSection(sectionId: number) {
  defaultSectionId.value = sectionId

  // Call the API to set the default section
  await setDefaultSection(sectionId)

  settingsStore.setDefaultSectionId(sectionId)

  ElMessage.success('Default section updated successfully')
}

const inviteStudentDialogVisible = ref(false)

const inviteStudentSectionId = ref<number>(NaN) // The section ID for which students are being invited
const inviteStudentCourseId = ref<number>(NaN) // The course ID for which students are being invited

function showInviteStudentsDialog(section: Section) {
  inviteStudentDialogVisible.value = true
  inviteStudentSectionId.value = section.sectionId as number
  inviteStudentCourseId.value = section.courseId as number
}

function closeInviteUsersDialog() {
  inviteStudentDialogVisible.value = false
  inviteStudentSectionId.value = NaN
  inviteStudentCourseId.value = NaN
}

// async function deleteExistingSection(existingSection: Section) {
//   ElMessageBox.confirm(
//     `${existingSection.sectionName} will be permanently deleted. Continue?`,
//     'Warning',
//     {
//       confirmButtonText: 'OK',
//       cancelButtonText: 'Cancel',
//       type: 'warning'
//     }
//   )
//     .then(() => deleteSection(existingSection.sectionId as number))
//     .then(() => {
//       ElMessage.success('Section deleted successfully')
//       loadSections()
//     })
//     .catch(() => {
//       ElMessage.info('Deletion canceled')
//     })
// }
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
  box-sizing: border-box;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
