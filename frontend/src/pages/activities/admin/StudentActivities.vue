<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Student Weekly Activities</span>
      </div>
    </template>
    <!-- Search Form -->
    <SearchPeriod
      v-model:selectedStartWeekNumber="searchCriteria.startWeek"
      v-model:selectedEndWeekNumber="searchCriteria.endWeek"
      @search="loadActivities"
      @reset="resetSearchCriteria"
    />
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="loadActivities">
      <el-tab-pane label="Details" name="details">
        <!-- Activity Table -->
        <el-table
          :data="activities"
          style="width: 100%"
          stripe
          border
          v-loading="loading"
          height="600"
        >
          <el-table-column label="Week" prop="week"></el-table-column>
          <el-table-column label="Category" width="150" prop="category">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.category }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Activity" width="150" prop="activity"></el-table-column>
          <el-table-column label="Description" prop="description"> </el-table-column>
          <el-table-column label="Planned Hours" prop="plannedHours"> </el-table-column>
          <el-table-column label="Actual Hours" prop="actualHours"> </el-table-column>
          <el-table-column label="Status" prop="status">
            <template #default="{ row }">
              <el-tag :type="row.status == 'COMPLETED' ? 'success' : 'danger'" size="small">
                {{ row.status }}
              </el-tag>
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
      </el-tab-pane>
      <el-tab-pane label="Categories" name="categories">
        <Bar :data="categoryData" :options="barChartOptions" height="500" width="1600" />
      </el-tab-pane>
      <el-tab-pane label="Statuses" name="statuses">
        <Doughnut :data="statusData" :options="doughnutChartOptions" height="500" />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { searchActivities } from '@/apis/activity'
import {
  ActivityCategory,
  ActivityStatus,
  type Activity,
  type ActivitySearchCriteria,
  type SearchActivityByCriteriaResponse
} from '@/apis/activity/types'
import { getCurrentWeekNumber } from '@/utils/week'
import SearchPeriod from '@/components/SearchPeriod.vue'

const { studentId } = defineProps(['studentId'])

const searchCriteria = ref<ActivitySearchCriteria>({
  studentId: studentId,
  startWeek: '',
  endWeek: ''
})

const activities = ref<Activity[]>([]) // A student's activities in a week

const activeName = ref<string>('details') // The active tab name

const loading = ref<boolean>(true) // Loading status

// Pagination information
const pageNumber = ref<number>(1) // current page number, starting from 1
const pageSize = ref<number>(50) // number of elements per page
const totalElements = ref<number>(50) // total number of elements

// Load current week's data when the component is mounted
onMounted(() => {
  searchCriteria.value.startWeek = getCurrentWeekNumber() // Get the current week
  searchCriteria.value.endWeek = getCurrentWeekNumber()
  // Get the current week
  loadActivities()
})

// Load activities based on the search criteria
async function loadActivities() {
  loading.value = true
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: SearchActivityByCriteriaResponse = await searchActivities(
    { page: pageNumber.value - 1, size: pageSize.value, sort: 'week,desc' },
    searchCriteria.value
  )
  activities.value = result.data.content

  // Update pagination information
  pageNumber.value = result.data.number + 1 // The page number starts from 0 on the back end, so add 1 here
  pageSize.value = result.data.size
  totalElements.value = result.data.totalElements
  loading.value = false
}

function resetSearchCriteria() {
  searchCriteria.value = {
    studentId,
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

import { Bar, Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  Title,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend, Title, BarElement, CategoryScale, LinearScale)

// Loop through Object.values(ActivityCategory) to get the labels
const categories = Object.values(ActivityCategory) as string[]

// Computed properties for chart data and options
const categoryData = computed(() => {
  const categoryCounts = categories.map((category) => {
    return activities.value.filter((activity) => activity.category === category).length
  })
  return {
    labels: categories,
    datasets: [
      {
        label: 'Activities',
        data: categoryCounts,
        backgroundColor: ['#66b1ff']
      }
    ]
  }
})

const statuses = Object.values(ActivityStatus) as string[]

const statusData = computed(() => {
  const statusCounts = statuses.map((status) => {
    return activities.value.filter((activity) => activity.status === status).length
  })
  return {
    labels: statuses,
    datasets: [
      {
        label: 'Activities',
        data: statusCounts,
        backgroundColor: ['#ebb563', '#85ce61']
      }
    ]
  }
})

const barChartOptions = {
  responsive: false,
  maintainAspectRatio: false,
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        precision: 0 // Ensure that y-axis ticks are whole numbers
      }
    }
  },
  plugins: {
    legend: {
      position: 'top'
    },
    title: {
      display: true,
      text: 'Activities by Category'
    }
  }
}

const doughnutChartOptions = {
  responsive: false,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top'
    },
    title: {
      display: true,
      text: 'Activities by Status'
    }
  }
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
