<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Student Management</span>
      </div>
    </template>
    <!-- Search Form -->
    <el-form inline>
      <el-form-item label="Student First Name:">
        <el-input v-model="searchCriteria.firstName" @input="loadStudents" />
      </el-form-item>
      <el-form-item label="Student Last Name:">
        <el-input v-model="searchCriteria.lastName" @input="loadStudents" />
      </el-form-item>
      <el-form-item>
        <!-- <el-button type="primary" @click="loadStudents">Query</el-button> -->
        <el-button @click="resetSearchCriteria">Reset</el-button>
      </el-form-item>
    </el-form>
    <!-- Student Table -->
    <el-table
      :data="students"
      style="width: 100%"
      stripe
      border
      v-loading="loading"
      @row-click="goToStudentPerformanceDashboard"
      height="600"
    >
      <el-table-column label="Id" prop="id"></el-table-column>
      <el-table-column label="First Name" prop="firstName"></el-table-column>
      <el-table-column label="Last Name" prop="lastName"></el-table-column>
      <el-table-column label="Email" prop="email"></el-table-column>
      <el-table-column label="Team" prop="teamName"></el-table-column>
      <el-table-column label="Status" prop="enabled">
        <template #default="{ row }">
          <el-tag type="success" v-if="row.enabled">Enabled</el-tag>
          <el-tag type="danger" v-else>Disabled</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Operations" width="120">
        <template #default="{ row }">
          <el-button
            icon="Edit"
            circle
            plain
            type="primary"
            @click.stop="showEditStudentDialog(row)"
          ></el-button>
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
      :page-sizes="[10, 50, 100]"
      layout="jumper, total, sizes, prev, pager, next"
      background
      :total="totalElements"
      @size-change="handlePageSizeChange"
      @current-change="handlePageNumberChange"
      style="margin-top: 20px; justify-content: flex-end"
    />
    <!-- Dialog for editing student -->
    <el-dialog v-model="dialogVisible" title="Edit a student" width="30%" destroy-on-close>
      <EditStudentForm
        :studentId="studentBeingEditedId"
        @close-dialog="closeDialog"
      ></EditStudentForm>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { searchStudents } from '@/apis/student'
import type {
  SearchStudentByCriteriaResponse,
  Student,
  StudentSearchCriteria
} from '@/apis/student/types'
import { useSettingsStore } from '@/stores/settings'
import EditStudentForm from './EditStudentForm.vue'
import { ElMessage } from 'element-plus'

const searchCriteria = ref<StudentSearchCriteria>({
  sectionId: NaN,
  firstName: '',
  lastName: ''
})

const students = ref<Student[]>([]) // Array of loaded students
const loading = ref<boolean>(true) // Loading status
const settingsStore = useSettingsStore()

// Pagination information
const pageNumber = ref<number>(1) // current page number, starting from 1
const pageSize = ref<number>(50) // number of elements per page
const totalElements = ref<number>(60) // total number of elements

// Load data when the component is mounted
onMounted(() => {
  searchCriteria.value.sectionId = settingsStore.defaultSectionId
  if (Number.isNaN(searchCriteria.value.sectionId)) {
    ElMessage.error('Please select a section in the Sections page.')
    return
  }
  loadStudents()
})

async function loadStudents() {
  loading.value = true
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: SearchStudentByCriteriaResponse = await searchStudents(
    { page: pageNumber.value - 1, size: pageSize.value },
    searchCriteria.value
  )
  students.value = result.data.content

  // Update pagination information
  pageNumber.value = result.data.number + 1 // The page number starts from 0 on the back end, so add 1 here
  pageSize.value = result.data.size
  totalElements.value = result.data.totalElements
  loading.value = false
}

function resetSearchCriteria() {
  searchCriteria.value = {
    sectionId: settingsStore.defaultSectionId,
    firstName: '',
    lastName: ''
  }
  loadStudents()
}

function handlePageNumberChange(newPageNumer: number) {
  pageNumber.value = newPageNumer // Not necessary since pageNumber already has a two-way binding
  loadStudents()
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize // Not necessary since pageSize already has a two-way binding
  pageNumber.value = 1 // Reset the page number to 1 when the page size changes
  loadStudents()
}

// Dialog for adding or editing artifacts
const dialogVisible = ref(false)

const studentBeingEditedId = ref<number>() // ID of the artifact being edited, it will be passed to the EditArtifactForm component as a prop

function showEditStudentDialog(student: Student) {
  dialogVisible.value = true
  studentBeingEditedId.value = student.id as number
}

function closeDialog() {
  dialogVisible.value = false
  loadStudents()
}

import { useRouter } from 'vue-router'

const router = useRouter()

function goToStudentPerformanceDashboard(student: Student) {
  // Redirect to the student performance dashboard page
  // The student ID can be passed as a query parameter
  router.push({
    name: 'student-performance-dashboard',
    params: { studentId: student.id }
  })
}
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
