<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Section's Weekly Activities</span>
      </div>
    </template>
    <!-- Search Form -->
    <SearchWeek
      v-model="activitySearchCriteria.week"
      @search="updateWeeklyActivities"
      @reset="resetSearchCriteria"
    />
    <!-- Team Table -->
    <el-table :data="teams" style="width: 100%" stripe border v-loading="loading" height="600">
      <el-table-column type="expand">
        <template #default="{ row }">
          <el-table :data="row.activitiesInAWeek" style="width: 100%" stripe border>
            <el-table-column label="Student" width="150" prop="studentName"></el-table-column>
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
            <el-table-column label="Comments" width="120">
              <template #default="{ row }">
                <el-button
                  v-if="row.comments && row.comments.length > 0"
                  icon="Comment"
                  circle
                  plain
                  type="primary"
                  @click="showComments(row)"
                ></el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="No data is available." />
            </template>
          </el-table>
          <!-- List the names of students who have not submitted activities -->
          <div v-if="row.studentsMissingActivity.length > 0">
            <el-divider></el-divider>

            <div style="display: flex; gap: 10px">
              <div>Students who have not submitted activities:</div>
              <el-tag
                type="danger"
                v-for="student in row.studentsMissingActivity"
                :key="student.id"
              >
                {{ student.firstName }} {{ student.lastName }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Id" prop="teamId"></el-table-column>
      <el-table-column label="Name" prop="teamName"> </el-table-column>
      <el-table-column label="Description" prop="description"> </el-table-column>
      <el-table-column label="Website URL" prop="teamWebsiteUrl"> </el-table-column>
      <template #empty>
        <el-empty description="No data is available." />
      </template>
    </el-table>
    <el-dialog v-model="commentDialogVisible" title="Activity Comments" width="30%">
      {{ currentActivity?.comments }}
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="commentDialogVisible = false"> Close </el-button>
        </span>
      </template>
    </el-dialog>
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
import { useSettingsStore } from '@/stores/settings'
import type {
  SearchStudentByCriteriaResponse,
  Student,
  StudentSearchCriteria
} from '@/apis/student/types'
import type { SearchTeamByCriteriaResponse, Team, TeamSearchCriteria } from '@/apis/team/types'
import { searchTeams } from '@/apis/team'
import { searchStudents } from '@/apis/student'
import SearchWeek from '@/components/SearchWeek.vue'
import { ElMessage } from 'element-plus'

const teamSearchCriteria = ref<TeamSearchCriteria>({
  sectionId: NaN
})

const activitySearchCriteria = ref<ActivitySearchCriteria>({
  week: ''
})

const studentSearchCriteria = ref<StudentSearchCriteria>({
  sectionId: NaN
})

const teams = ref<Team[]>([]) // Teams that meet the search criteria
const activities = ref<Activity[]>([]) // The section's activities in a week
const students = ref<Student[]>([]) // All students in the section
const loading = ref<boolean>(true)
const settingsStore = useSettingsStore()

// Load data when the component is mounted
onMounted(async () => {
  activitySearchCriteria.value.week = getCurrentWeekNumber()

  studentSearchCriteria.value.sectionId = settingsStore.defaultSectionId
  if (!studentSearchCriteria.value.sectionId) {
    ElMessage.error('Please select a section in the Sections page.')
    return
  }
  prepareSectionWeeklyActivities()
})

// Load teams based on the search criteria
async function loadTeams() {
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: SearchTeamByCriteriaResponse = await searchTeams(
    { page: 0, size: 100 },
    teamSearchCriteria.value
  )
  teams.value = result.data.content
}

// Load activities based on the search criteria
async function loadActivities() {
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: SearchActivityByCriteriaResponse = await searchActivities(
    { page: 0, size: 200 },
    activitySearchCriteria.value
  )
  activities.value = result.data.content // Activities that meet the search criteria

  // Group activities by teamId
  const activitiesGroupedByTeamId = activities.value.reduce(
    (team, activity) => {
      if (!team[activity.teamId!]) {
        team[activity.teamId!] = []
      }
      team[activity.teamId!].push(activity)
      return team
    },
    {} as Record<number, Activity[]>
  )

  // Assign activities to teams
  teams.value.forEach((team) => {
    team.activitiesInAWeek = activitiesGroupedByTeamId[team.teamId!] || []
    // Sort activities by student name
    team.activitiesInAWeek.sort((a, b) => a.studentName!.localeCompare(b.studentName!))
  })
}

async function loadStudents() {
  const result: SearchStudentByCriteriaResponse = await searchStudents(
    {
      page: 0,
      size: 100
    },
    studentSearchCriteria.value
  )
  students.value = result.data.content

  // Find out which students have not submitted activities
  const studentsNotSubmitingActivities = students.value.filter((student) => {
    return !activities.value.some((activity) => activity.studentId === student.id)
  })

  // Group students by teamId
  const studentsNotSubmitingActivitiesGroupedByTeamId = studentsNotSubmitingActivities.reduce(
    (team, student) => {
      if (!team[student.teamId!]) {
        team[student.teamId!] = []
      }
      team[student.teamId!].push(student)
      return team
    },
    {} as Record<number, Student[]>
  )

  // Assign students to teams
  teams.value.forEach((team) => {
    team.studentsMissingActivity = studentsNotSubmitingActivitiesGroupedByTeamId[team.teamId!] || []
  })
}

async function prepareSectionWeeklyActivities() {
  loading.value = true
  await loadTeams()
  await loadActivities()
  await loadStudents()
  loading.value = false
}

async function updateWeeklyActivities() {
  prepareSectionWeeklyActivities()
}

async function resetSearchCriteria() {
  activitySearchCriteria.value = {
    week: getCurrentWeekNumber()
  }
  prepareSectionWeeklyActivities()
}

const commentDialogVisible = ref<boolean>(false)
const currentActivity = ref<Activity>()
function showComments(activity: Activity) {
  currentActivity.value = activity
  commentDialogVisible.value = true
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
