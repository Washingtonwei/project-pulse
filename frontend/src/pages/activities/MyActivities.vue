<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>My Weekly Activities</span>
        <div class="extra">
          <el-button
            type="primary"
            @click="showAddActivityForm()"
            icon="Plus"
            :disabled="(userInfoStore.userInfo as Student).teamId == null"
          >
            Add new activity
          </el-button>
        </div>
      </div>
    </template>
    <!-- Search Form -->
    <SearchWeek
      v-model="searchCriteria.week"
      @search="loadActivities"
      @currentWeek="loadActivitiesForCurrentWeek"
    />
    <!-- Activity Table -->
    <el-table
      :data="activities"
      style="width: 100%"
      stripe
      border
      v-loading="loading"
      scrollbar-always-on
    >
      <el-table-column label="Category" min-width="150" prop="category">
        <template #default="{ row }">
          <el-tag type="warning">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Activity" min-width="150" prop="activity"></el-table-column>
      <el-table-column label="Description" min-width="150" prop="description"> </el-table-column>
      <el-table-column label="Planned Hours" min-width="150" prop="plannedHours"> </el-table-column>
      <el-table-column label="Actual Hours" min-width="150" prop="actualHours"> </el-table-column>
      <el-table-column label="Status" min-width="150" prop="status">
        <template #default="{ row }">
          <el-tag :type="row.status == 'COMPLETED' ? 'success' : 'danger'" size="small">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Operations" min-width="120">
        <template #default="{ row }">
          <el-button
            icon="Edit"
            circle
            plain
            type="primary"
            @click="showEditActivityForm(row)"
          ></el-button>
          <el-button
            icon="Delete"
            circle
            plain
            type="danger"
            @click="deleteExistingActivity(row)"
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
      :page-sizes="[2, 5, 10]"
      layout="jumper, total, sizes, prev, pager, next"
      background
      :total="totalElements"
      @size-change="handlePageSizeChange"
      @current-change="handlePageNumberChange"
      style="margin-top: 20px; justify-content: flex-end"
    />
    <!-- Drawer for adding or editing activities -->
    <el-drawer
      :title="drawerTitle"
      direction="rtl"
      size="35%"
      v-model="drawerVisible"
      destroy-on-close
    >
      <AddActivityForm
        v-if="drawerTitle == 'Add an activity'"
        :week="searchCriteria.week"
        @close-drawer="closeDrawer"
      ></AddActivityForm>
      <EditActivityForm
        v-if="drawerTitle == 'Edit an activity'"
        :activityId="activityBeingEditedId"
        @close-drawer="closeDrawer"
      ></EditActivityForm>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { searchActivities, deleteActivity } from '@/apis/activity'
import type {
  Activity,
  ActivitySearchCriteria,
  SearchActivityByCriteriaResponse
} from '@/apis/activity/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddActivityForm from './AddActivityForm.vue'
import EditActivityForm from './EditActivityForm.vue'
import { getCurrentWeekNumber } from '@/utils/week'
import { useUserInfoStore } from '@/stores/userInfo'
import SearchWeek from '@/components/SearchWeek.vue'
import type { Student } from '@/apis/student/types'

const searchCriteria = ref<ActivitySearchCriteria>({
  studentId: NaN,
  week: ''
})

const activities = ref<Activity[]>([]) // A student's activities in a week

const loading = ref<boolean>(true) // Loading status

// Pagination information
const pageNumber = ref<number>(1) // current page number, starting from 1
const pageSize = ref<number>(10) // number of elements per page
const totalElements = ref<number>(10) // total number of elements

const userInfoStore = useUserInfoStore() // Need student's ID to search for activities

// Load current week's data when the component is mounted
onMounted(() => {
  searchCriteria.value.studentId = userInfoStore.userInfo?.id as number
  // Get the current week
  searchCriteria.value.week = getCurrentWeekNumber()
  loadActivities()
})

// Load activities based on the search criteria
async function loadActivities() {
  loading.value = true
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: SearchActivityByCriteriaResponse = await searchActivities(
    { page: pageNumber.value - 1, size: pageSize.value },
    searchCriteria.value
  )
  activities.value = result.data.content

  // Update pagination information
  pageNumber.value = result.data.number + 1 // The page number starts from 0 on the back end, so add 1 here
  pageSize.value = result.data.size
  totalElements.value = result.data.totalElements
  loading.value = false
}

function loadActivitiesForCurrentWeek() {
  searchCriteria.value = {
    studentId: userInfoStore.userInfo?.id as number,
    week: getCurrentWeekNumber()
  }
  loadActivities()
}

function handlePageNumberChange(newPageNumer: number) {
  pageNumber.value = newPageNumer // Not necessary since pageNumber already has a two-way binding
  loadActivities()
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize // Not necessary since pageSize already has a two-way binding
  pageNumber.value = 1 // Reset the page number to 1 when the page size changes
  loadActivities()
}

// Drawer for adding or editing activities
const drawerVisible = ref<boolean>(false)
const drawerTitle = ref<string>('')

function showAddActivityForm() {
  drawerTitle.value = 'Add an activity'
  drawerVisible.value = true
}

const activityBeingEditedId = ref<number>(NaN) // ID of the activity being edited, it will be passed to the EditActivityForm component as a prop

function showEditActivityForm(activity: Activity) {
  drawerTitle.value = 'Edit an activity'
  activityBeingEditedId.value = activity.activityId as number
  drawerVisible.value = true
}

function closeDrawer() {
  drawerVisible.value = false
  loadActivities()
}

async function deleteExistingActivity(existingActivity: Activity) {
  ElMessageBox.confirm(
    `${existingActivity.activity} will be permanently deleted. Continue?`,
    'Warning',
    {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  )
    .then(() => deleteActivity(existingActivity.activityId as number))
    .then(() => {
      ElMessage.success('Activity deleted successfully')
      loadActivities()
    })
    .catch(() => {
      ElMessage.info('Deletion canceled')
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
