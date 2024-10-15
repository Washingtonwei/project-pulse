<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Submit Peer Evaluations for week: {{ previousWeekRange }}</span>
      </div>
    </template>
    <!-- Evaluation Table, adding height can fix the table header -->
    <el-table
      :data="peerEvalutions"
      style="width: 100%"
      stripe
      border
      height="600"
      v-loading="loading"
      scrollbar-always-on
    >
      <el-table-column
        label="Student Name"
        prop="evaluateeName"
        min-width="100"
        fixed
      ></el-table-column>
      <el-table-column v-for="criterion in criteria" :key="criterion.criterionId" min-width="150">
        <template #header>
          <el-tooltip effect="dark" :content="criterion.criterion" placement="top">
            <span>{{ criterion.description }}</span>
          </el-tooltip>
        </template>
        <template #default="{ row }">
          <div style="display: flex; justify-content: center; align-items: center">
            <el-input-number
              v-model="
                row.ratings.find((r: Rating) => r.criterionId === criterion.criterionId).actualScore
              "
              :min="0"
              :max="criterion.maxScore"
              :step="1"
              controls-position="right"
              style="width: 80px"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column min-width="200px">
        <template #header>
          <el-tooltip
            effect="dark"
            content="Public comments are available to the evaluatee."
            placement="top"
          >
            <span>Public Comment</span>
          </el-tooltip>
        </template>
        <template #default="{ row }">
          <el-input
            v-model="row.publicComment"
            type="textarea"
            :rows="5"
            placeholder="Enter public comment"
            max-length="255"
            show-word-limit
          />
        </template>
      </el-table-column>
      <el-table-column min-width="200px">
        <template #header>
          <el-tooltip
            effect="dark"
            content="Private comments are ONLY available to the instructor."
            placement="top"
          >
            <span>Private Comment</span>
          </el-tooltip>
        </template>
        <template #default="{ row }">
          <el-input
            v-model="row.privateComment"
            type="textarea"
            :rows="5"
            placeholder="Enter private comment"
            max-length="255"
            show-word-limit
          />
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="No data is available." />
      </template>
    </el-table>
    <!-- submit button on the right side-->
    <div style="display: flex; justify-content: flex-end">
      <el-button
        type="primary"
        @click="submitPeerEvaluations"
        v-if="isNewSubmission"
        :loading="isLoading"
        >Submit</el-button
      >
      <el-button type="warning" @click="updatePeerEvaluations" :loading="isLoading" v-else
        >Update</el-button
      >
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { findStudentsByTeamId } from '@/apis/student'
import type { Student, FindStudentByTeamIdResponse } from '@/apis/student/types'
import { findSectionById } from '@/apis/section'
import { findRubricById } from '@/apis/rubric'
import type { FindRubricByIdResponse, Criterion } from '@/apis/rubric/types'
import { getPreviousWeek, getPreviousWeekRange } from '@/utils/week'
import { useUserInfoStore } from '@/stores/userInfo'
import type { FindSectionByIdResponse } from '@/apis/section/types'
import {
  submitEvaluation,
  getEvaluationsByEvaluatorIdAndWeek,
  updateEvaluation
} from '@/apis/evaluation'
import { ElMessage } from 'element-plus'
import type {
  FetchPeerEvaluationsResponse,
  PeerEvaluation,
  Rating,
  SubmitPeerEvaluationResponse,
  UpdatePeerEvaluationResponse
} from '@/apis/evaluation/types'

const previousWeek = ref<string>('')
const previousWeekRange = ref<string>('')

const peerEvalutions = ref<PeerEvaluation[]>()

const loading = ref<boolean>(false)

let isNewSubmission = true

const teamId = ref<number>(NaN) // Team ID of the student

const students = ref<Student[]>([]) // Students on the same team
const sectionId = ref<number>() // Section ID of the team
const criteria = ref<Criterion[]>([]) // Criteria used for peer evaluations
const isLoading = ref(false)

const userInfoStore = useUserInfoStore() // Need team ID to search for students

// Load data when the component is mounted
onMounted(async () => {
  previousWeek.value = getPreviousWeek()
  previousWeekRange.value = getPreviousWeekRange()
  teamId.value = (userInfoStore.userInfo as Student).teamId as number
  if (!teamId.value) {
    ElMessage.error('You have not been assigned a team yet.')
    return
  }
  await loadStudents()
  sectionId.value = (userInfoStore.userInfo as Student).sectionId
  await loadCriteria()
  loading.value = true
  prepareStartingPeerEvaluations()
  loading.value = false
})

// Load activities based on the search criteria
async function loadStudents() {
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: FindStudentByTeamIdResponse = await findStudentsByTeamId(teamId.value)
  students.value = result.data
}

// Load criteria information
async function loadCriteria() {
  const sectionResult: FindSectionByIdResponse = await findSectionById(sectionId.value as number)
  const rubricResult: FindRubricByIdResponse = await findRubricById(
    sectionResult.data.rubricId as number
  )
  criteria.value = rubricResult.data.criteria!
}

// Create starting peer evaluations
async function prepareStartingPeerEvaluations() {
  // Get the evaluations for the current week
  const result: FetchPeerEvaluationsResponse = await getEvaluationsByEvaluatorIdAndWeek(
    (userInfoStore.userInfo as Student).id as number,
    previousWeek.value // Hardcoded week for now
  )

  // If the student has already submitted evaluations for the current week, load them
  if (result.data.length > 0) {
    peerEvalutions.value = result.data
    isNewSubmission = false
  } else {
    isNewSubmission = true
    peerEvalutions.value = students.value.map((student) => ({
      week: previousWeek.value,
      evaluatorId: (userInfoStore.userInfo as Student).id as number,
      evaluateeId: student.id as number,
      evaluateeName: `${student.firstName} ${student.lastName}`,
      ratings: criteria.value.map((criterion) => ({
        criterionId: criterion.criterionId as number,
        criterion: criterion.criterion,
        actualScore: 0
      })),
      publicComment: '',
      privateComment: ''
    }))
  }
}

// Submit peer evaluations
async function submitPeerEvaluations() {
  if (peerEvalutions.value) {
    const submitPromises = peerEvalutions.value.map(async (evaluation) => {
      const result: SubmitPeerEvaluationResponse = await submitEvaluation(evaluation)
      return result.data
    })
    isLoading.value = true
    peerEvalutions.value = await Promise.all(submitPromises) // Wait for all promises to resolve
    isNewSubmission = false // Change to update mode
    ElMessage.success('Peer evaluations submitted successfully')
    isLoading.value = false
  }
}

// Update peer evaluations
async function updatePeerEvaluations() {
  if (peerEvalutions.value) {
    const submitPromises = peerEvalutions.value.map(async (evaluation) => {
      const result: UpdatePeerEvaluationResponse = await updateEvaluation(evaluation)
      return result.data
    })

    isLoading.value = true
    peerEvalutions.value = await Promise.all(submitPromises) // Wait for all promises to resolve
    ElMessage.success('Peer evaluations updated successfully')
    isLoading.value = false
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
