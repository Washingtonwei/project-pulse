<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Team's Weekly Activities</span>
      </div>
    </template>
    <!-- Search Form -->
    <SearchWeek
      v-model="searchCriteria.week"
      @search="loadActivities"
      @reset="resetSearchCriteria"
    />
    <!-- Activity Table -->
    <div v-for="(activities, studentName) in groupedActivities" :key="studentName">
      <h3>{{ studentName }}'s Weekly Activities:</h3>
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
        <el-table-column label="Planned Hours" min-width="150" prop="plannedHours">
        </el-table-column>
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
              icon="Comment"
              circle
              plain
              type="primary"
              @click="showCommentActivityForm(row)"
            ></el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="No data is available." />
        </template>
      </el-table>
    </div>
    <!-- Drawer for adding or editing activities -->
    <el-drawer
      title="Comment an activity"
      direction="rtl"
      size="35%"
      v-model="drawerVisible"
      destroy-on-close
    >
      <CommentActivityForm
        :activityId="activityBeingCommentedId"
        @close-drawer="closeDrawer"
      ></CommentActivityForm>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { searchActivities } from '@/apis/activity'
import type {
  Activity,
  ActivitySearchCriteria,
  SearchActivityByCriteriaResponse
} from '@/apis/activity/types'
import { getCurrentWeekNumber } from '@/utils/week'
import { useUserInfoStore } from '@/stores/userInfo'
import type { Student } from '@/apis/student/types'
import CommentActivityForm from './CommentActivityForm.vue'
import SearchWeek from '@/components/SearchWeek.vue'
import { ElMessage } from 'element-plus'

const searchCriteria = ref<ActivitySearchCriteria>({
  teamId: NaN,
  week: ''
})

const activities = ref<Activity[]>([]) // The team's activities in a week
const groupedActivities = ref<Record<string, Activity[]>>({}) // Grouped activities by studentName
const loading = ref<boolean>(true) // Loading status

// Pagination information
const pageNumber = ref<number>(1) // current page number, starting from 1
const pageSize = ref<number>(100) // number of elements per page
const totalElements = ref<number>(100) // total number of elements

const userInfoStore = useUserInfoStore() // Need team ID to search activities

// Load data when the component is mounted
onMounted(() => {
  searchCriteria.value.teamId = (userInfoStore.userInfo as Student).teamId
  if (!searchCriteria.value.teamId) {
    ElMessage.error('You have not been assigned a team yet.')
    return
  }
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
  activities.value = result.data.content // Activities that meet the search criteria

  // Group activities by studentName
  groupedActivities.value = activities.value.reduce(
    (group, activity) => {
      const studentName = activity.studentName || 'Unknown'
      if (!group[studentName]) {
        group[studentName] = []
      }
      group[studentName].push(activity)
      return group
    },
    {} as Record<string, Activity[]>
  )

  // Update pagination information
  pageNumber.value = result.data.number + 1 // The page number starts from 0 on the back end, so add 1 here
  pageSize.value = result.data.size
  totalElements.value = result.data.totalElements

  loading.value = false
}

function resetSearchCriteria() {
  searchCriteria.value = {
    teamId: (userInfoStore.userInfo as Student).teamId,
    week: getCurrentWeekNumber()
  }
  loadActivities()
}

const drawerVisible = ref<boolean>(false)
const activityBeingCommentedId = ref<number>(NaN)

function showCommentActivityForm(activity: Activity) {
  activityBeingCommentedId.value = activity.activityId as number
  drawerVisible.value = true
}

function closeDrawer() {
  drawerVisible.value = false
  loadActivities()
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
