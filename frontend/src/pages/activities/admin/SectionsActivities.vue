<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Section's Weekly Activities</span>
        <el-button
          type="primary"
          icon="Bell"
          :disabled="Number.isNaN(settingsStore.defaultSectionId)"
          @click="remindDialogVisible = true"
          data-cy="remind-open"
        >
          Remind students who have not submitted
        </el-button>
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
          <el-table
            :data="row.activitiesInAWeek"
            style="width: 100%"
            stripe
            border
            scrollbar-always-on
          >
            <el-table-column
              label="Student"
              width="150"
              min-width="100"
              prop="studentName"
              fixed
            ></el-table-column>
            <el-table-column label="Category" width="150" prop="category">
              <template #default="{ row }">
                <el-tag type="warning">{{ row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Activity" min-width="150" prop="activity"></el-table-column>
            <el-table-column label="Description" min-width="150" prop="description">
            </el-table-column>
            <el-table-column label="Planned Hours" min-width="150" prop="plannedHours">
            </el-table-column>
            <el-table-column label="Actual Hours" min-width="150" prop="actualHours">
            </el-table-column>
            <el-table-column label="Status" min-width="150" prop="status">
              <template #default="{ row }">
                <el-tag :type="row.status == 'COMPLETED' ? 'success' : 'danger'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Peer Evaluations" width="120">
              <template #default="{ row }">
                <el-button
                  icon="Aim"
                  circle
                  plain
                  type="primary"
                  @click="showPeerEvaluationComments(row)"
                ></el-button>
              </template>
            </el-table-column>
            <!-- createdAt  -->
            <el-table-column label="Created At" min-width="150" prop="createdAt"></el-table-column>
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
    <!-- Weekly Evaluations Dialog for a Student (Comments only) -->
    <el-dialog v-model="peerEvalDialogVisible" :title="peerEvalDialogTitle" width="80%">
      <el-table
        :data="detailedWeeklyEvaluations"
        style="width: 100%"
        stripe
        border
        scrollbar-always-on
      >
        <el-table-column
          label="Evaluator"
          prop="evaluatorName"
          min-width="100"
          fixed
        ></el-table-column>
        <el-table-column label="Public Comment" min-width="200px">
          <template #default="{ row }">
            {{ row.publicComment }}
          </template>
        </el-table-column>
        <el-table-column label="Private Comment" min-width="200px">
          <template #default="{ row }">
            {{ row.privateComment }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="peerEvalDialogVisible = false"> Cancel </el-button>
        </span>
      </template>
    </el-dialog>
    <RemindNonSubmittersDialog
      v-model="remindDialogVisible"
      :section-id="settingsStore.defaultSectionId"
    />
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
import { getPreviousWeek } from '@/utils/week'
import { useSettingsStore } from '@/stores/settings'
import type { SearchTeamByCriteriaResponse, Team, TeamSearchCriteria } from '@/apis/team/types'
import { searchTeams } from '@/apis/team'
import { getSubmissionStatus } from '@/apis/notification'
import type { GetSubmissionStatusResponse } from '@/apis/notification/types'
import SearchWeek from '@/components/SearchWeek.vue'
import RemindNonSubmittersDialog from '@/components/RemindNonSubmittersDialog.vue'
import { ElMessage } from 'element-plus'
import type {
  GetWeeklyPeerEvaluationsForStudentResponse,
  PeerEvaluation
} from '@/apis/evaluation/types'
import { getWeeklyPeerEvaluationsForStudent } from '@/apis/evaluation'

const teamSearchCriteria = ref<TeamSearchCriteria>({
  sectionId: NaN
})

const activitySearchCriteria = ref<ActivitySearchCriteria>({
  week: ''
})

const teams = ref<Team[]>([]) // Teams that meet the search criteria
const activities = ref<Activity[]>([]) // The section's activities in a week
const sectionId = ref<number>(NaN)
const loading = ref<boolean>(true)
const settingsStore = useSettingsStore()
const remindDialogVisible = ref(false)

// Load data when the component is mounted
onMounted(async () => {
  // A weekly activity report is about the previous week while it is due, so that is the week to open on
  activitySearchCriteria.value.week = getPreviousWeek()

  sectionId.value = settingsStore.defaultSectionId
  if (Number.isNaN(sectionId.value)) {
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

// Who has not submitted comes from the server (BR-submission-owed), already grouped by team. Computing it here
// from one page of activities and one page of students listed students who had submitted once a section grew.
async function loadStudentsMissingActivity() {
  // A cleared week searches every week's activities, and "has not submitted" only means something for one week
  if (!activitySearchCriteria.value.week) {
    teams.value.forEach((team) => (team.studentsMissingActivity = []))
    return
  }
  const result: GetSubmissionStatusResponse = await getSubmissionStatus(
    sectionId.value,
    'WEEKLY_ACTIVITY_REPORT',
    activitySearchCriteria.value.week
  )
  teams.value.forEach((team) => {
    team.studentsMissingActivity =
      result.data.teams.find((owingTeam) => owingTeam.teamId === team.teamId)?.students || []
  })
}

async function prepareSectionWeeklyActivities() {
  loading.value = true
  try {
    await loadTeams()
    await loadActivities()
    await loadStudentsMissingActivity()
  } finally {
    loading.value = false
  }
}

async function updateWeeklyActivities() {
  prepareSectionWeeklyActivities()
}

async function resetSearchCriteria() {
  activitySearchCriteria.value = {
    week: getPreviousWeek()
  }
  prepareSectionWeeklyActivities()
}

// Show detailed weekly evaluations for a student (comments only)
// This function is called when the user clicks the "Peer Eval" button in the table
const peerEvalDialogVisible = ref(false)
const peerEvalDialogTitle = ref('')
const detailedWeeklyEvaluations = ref<PeerEvaluation[]>([])

async function showPeerEvaluationComments(activity: Activity) {
  const result: GetWeeklyPeerEvaluationsForStudentResponse =
    await getWeeklyPeerEvaluationsForStudent(activity.studentId!, activity.week)
  detailedWeeklyEvaluations.value = result.data
  peerEvalDialogTitle.value = `${activity.studentName}'s Peer Evaluations`
  peerEvalDialogVisible.value = true
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
