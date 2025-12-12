<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Course Management</span>
        <div class="extra">
          <el-button type="primary" @click="showAddDialog()" icon="Plus" v-if="isAdmin">
            Add new course
          </el-button>
        </div>
      </div>
    </template>
    <!-- Search Form -->
    <el-form inline>
      <el-form-item label="Course Name:">
        <el-input v-model="searchCriteria.courseName" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadCourses">Query</el-button>
        <el-button @click="resetSearchCriteria">Reset</el-button>
      </el-form-item>
    </el-form>
    <!-- Course Table -->
    <el-table
      :data="courses"
      style="width: 100%"
      stripe
      border
      v-loading="loading"
      scrollbar-always-on
    >
      <el-table-column label="Default Course" min-width="100" align="center">
        <template #default="{ row }">
          <el-radio
            v-model="defaultCourseId"
            :value="row.courseId"
            @change="updateDefaultCourse(row.courseId)"
          ></el-radio>
        </template>
      </el-table-column>
      <el-table-column label="Id" prop="courseId" min-width="100"></el-table-column>
      <el-table-column label="Name" prop="courseName" min-width="150"> </el-table-column>
      <el-table-column label="Operations" min-width="150" v-if="isAdmin">
        <template #default="{ row }">
          <el-button
            icon="Edit"
            circle
            plain
            type="primary"
            @click="showEditDialog(row)"
          ></el-button>
          <!-- <el-button
            icon="Delete"
            circle
            plain
            type="danger"
            @click="deleteExistingCourse(row)"
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
    <!-- Dialog for adding/editing course -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form
        ref="courseForm"
        :model="courseData"
        :rules="rules"
        label-width="auto"
        style="padding-right: 30px"
        label-position="right"
      >
        <el-form-item label="Id:" v-if="dialogTitle == 'Edit a course'">
          <el-input v-model="courseData.courseId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name:" prop="courseName">
          <el-input v-model="courseData.courseName" minlength="1"></el-input>
        </el-form-item>
        <el-form-item label="Description:" prop="courseDescription">
          <el-input v-model="courseData.courseDescription" minlength="1"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false"> Cancel </el-button>
          <el-button
            type="primary"
            @click="dialogTitle == 'Add a course' ? addCourse() : updateExistingCourse()"
            :loading="buttonLoading"
          >
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { searchCourses, createCourse, updateCourse } from '@/apis/course'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import type {
  SearchCourseByCriteriaResponse,
  Course,
  CourseSearchCriteria
} from '@/apis/course/types'
import { useSettingsStore } from '@/stores/settings'
import { setDefaultCourse } from '@/apis/instructor'
import { useUserInfoStore } from '@/stores/userInfo'
import { storeToRefs } from 'pinia'

const userInfoStore = useUserInfoStore()
const { isAdmin } = storeToRefs(userInfoStore) // This preserves full reactivity.

const searchCriteria = ref<CourseSearchCriteria>({
  courseName: '',
  courseDescription: ''
})

const courses = ref<Course[]>([])
const loading = ref<boolean>(true)
const buttonLoading = ref<boolean>(false)

// Pagination information
const pageNumber = ref<number>(1) // current page number, starting from 1
const pageSize = ref<number>(10) // number of elements per page
const totalElements = ref<number>(10) // total number of elements

const courseForm = ref<FormInstance>()

// Load data when the component is mounted
onMounted(() => {
  loadCourses()
})

// Load activities based on the search criteria
async function loadCourses() {
  loading.value = true
  try {
    // On the back end, the page number starts from 0, so subtract 1 here
    const result: SearchCourseByCriteriaResponse = await searchCourses(
      { page: pageNumber.value - 1, size: pageSize.value },
      searchCriteria.value
    )
    courses.value = result.data.content

    // Update pagination information
    pageNumber.value = result.data.number + 1 // The page number starts from 0 on the back end, so add 1 here
    pageSize.value = result.data.size
    totalElements.value = result.data.totalElements
  } finally {
    loading.value = false
  }
}

function resetSearchCriteria() {
  searchCriteria.value = {
    courseName: '',
    courseDescription: ''
  }
  loadCourses()
}

function handlePageNumberChange(newPageNumer: number) {
  pageNumber.value = newPageNumer // Not necessary since pageNumber already has a two-way binding
  loadCourses()
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize // Not necessary since pageSize already has a two-way binding
  pageNumber.value = 1 // Reset the page number to 1 when the page size changes
  loadCourses()
}

// Control the visibility of the dialog
const dialogVisible = ref(false)

const courseData = ref<Course>({
  // Data of the course being added or edited
  courseId: NaN,
  courseName: '',
  courseDescription: ''
})

// Validation rules
const rules = {
  courseName: [
    { required: true, message: 'Please provide the name of the course.', trigger: 'blur' }
  ],
  courseDescription: [
    { required: true, message: 'Please provide the description of the course.', trigger: 'blur' }
  ]
}

function clearForm() {
  courseData.value = {
    courseId: NaN,
    courseName: '',
    courseDescription: ''
  }
}

const dialogTitle = ref<string>('')

function showAddDialog() {
  if (!isAdmin.value) {
    ElMessage.error('You do not have permission to create a course.')
    return
  }

  clearForm()
  courseForm.value?.clearValidate() // Clear the validation status of the form. The first time the dialog is opened, the form is not defined, so we need to check if it is defined before calling clearValidate()
  dialogTitle.value = 'Add a course'
  dialogVisible.value = true
}

async function addCourse() {
  if (!isAdmin.value) {
    ElMessage.error('You do not have permission to create a course.')
    return
  }

  await courseForm.value!.validate() // Validate the form

  buttonLoading.value = true
  const newCourse: Course = {
    courseName: courseData.value.courseName,
    courseDescription: courseData.value.courseDescription
  }

  await createCourse(newCourse)
  ElMessage.success('Course added successfully')
  buttonLoading.value = false

  // Close the dialog
  dialogVisible.value = false

  // Reload the course table
  loadCourses()
}

function showEditDialog(existingCourse: Course) {
  if (!isAdmin.value) {
    ElMessage.error('You do not have permission to update a course.')
    return
  }

  clearForm()
  courseForm.value?.clearValidate()
  dialogVisible.value = true
  dialogTitle.value = 'Edit a course'

  courseData.value.courseId = existingCourse.courseId as number
  courseData.value.courseName = existingCourse.courseName
  courseData.value.courseDescription = existingCourse.courseDescription
}

async function updateExistingCourse() {
  if (!isAdmin.value) {
    ElMessage.error('You do not have permission to update a course.')
    return
  }

  await courseForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const updatedCourse: Course = {
    courseId: courseData.value.courseId as number,
    courseName: courseData.value.courseName,
    courseDescription: courseData.value.courseDescription
  }

  // Call the API to update the course
  await updateCourse(updatedCourse)
  ElMessage.success('Course updated successfully')
  buttonLoading.value = false
  // Close the dialog
  dialogVisible.value = false

  // Reload the course table
  loadCourses()
}

const settingsStore = useSettingsStore()

const defaultCourseId = ref<number>(settingsStore.defaultCourseId)

async function updateDefaultCourse(courseId: number) {
  defaultCourseId.value = courseId

  // Call the API to set the default course
  await setDefaultCourse(courseId)

  // This is a user-initiated course switch, so reset the section
  settingsStore.setDefaultCourseIdAndResetSection(courseId)

  ElMessage.success('Default course updated successfully')
}

// async function deleteExistingCourse(existingCourse: Course) {
//   ElMessageBox.confirm(
//     `${existingCourse.courseName} will be permanently deleted. Continue?`,
//     'Warning',
//     {
//       confirmButtonText: 'OK',
//       cancelButtonText: 'Cancel',
//       type: 'warning'
//     }
//   )
//     .then(() => deleteCourse(existingCourse.courseId as number))
//     .then(() => {
//       ElMessage.success('Course deleted successfully')
//       loadCourses()
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
