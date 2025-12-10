<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Section's Peer Evaluations</span>
      </div>
    </template>
    <!-- Search Form -->
    <!-- Search Form -->
    <SearchWeek
      v-model="week"
      @search="loadSectionsPeerEvaluationAverages"
      @reset="resetSearchCriteria"
    />
    <!-- Evaluation Table -->
    <el-table
      :data="peerEvalutionAverages"
      style="width: 100%"
      stripe
      border
      v-loading="loading"
      height="600"
      scrollbar-always-on
    >
      <el-table-column label="Student" min-width="100px" fixed>
        <template #default="{ row }"> {{ row.lastName }}, {{ row.firstName }} </template>
      </el-table-column>
      <el-table-column label="Average Total Score" min-width="100px" fixed>
        <template #default="{ row }">
          {{ row.averageTotalScore.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column v-for="criterion in criteria" :key="criterion.criterionId" min-width="150px">
        <template #header>
          <el-tooltip effect="dark" :content="criterion.criterion" placement="top">
            <span>{{ criterion.description }}</span>
          </el-tooltip>
        </template>
        <template #default="{ row }">
          {{
            row.ratingAverages
              .find((r: RatingAverage) => r.criterionId === criterion.criterionId)
              ?.averageScore.toFixed(2) || ''
          }}
        </template>
      </el-table-column>
      <el-table-column label="Public Comments" min-width="200px">
        <template #default="{ row }">
          <p v-for="(comment, index) in row.publicComments" :key="index">
            {{ comment }}
          </p>
        </template>
      </el-table-column>
      <el-table-column label="Private Comments" min-width="200px">
        <template #default="{ row }">
          <p v-for="(comment, index) in row.privateComments" :key="index">
            {{ comment }}
          </p>
        </template>
      </el-table-column>
      <el-table-column label="Details" fixed="right">
        <template #default="{ row }">
          <el-button
            icon="Aim"
            circle
            plain
            type="primary"
            @click="showDetailedWeeklyEvaluations(row)"
          ></el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="No data is available." />
      </template>
    </el-table>
    <!-- Students Missing Peer Evaluations -->
    <div v-if="studentsMissingPeerEvaluations.length > 0">
      <el-divider></el-divider>

      <div style="display: flex; gap: 10px; align-items: center">
        <div>Students who have not submitted peer evaluations:</div>
        <div style="display: flex; flex-wrap: wrap; gap: 5px">
          <el-tag
            type="danger"
            v-for="(student, index) in studentsMissingPeerEvaluations"
            :key="index"
          >
            {{ student }}
          </el-tag>
        </div>
      </div>
    </div>
    <!-- Detailed Weekly Evaluations Dialog for a Student -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="80%">
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
        <el-table-column v-for="criterion in criteria" :key="criterion.criterionId" min-width="150">
          <template #header>
            <el-tooltip effect="dark" :content="criterion.criterion" placement="top">
              <span>{{ criterion.description }}</span>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            {{
              row.ratings
                .find((r: Rating) => r.criterionId === criterion.criterionId)
                .actualScore.toFixed(2)
            }}
          </template>
        </el-table-column>
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
          <el-button @click="dialogVisible = false"> Cancel </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { findSectionById } from '@/apis/section'
import { findRubricById } from '@/apis/rubric'
import type { FindRubricByIdResponse, Criterion } from '@/apis/rubric/types'
import { useSettingsStore } from '@/stores/settings'
import type { FindSectionByIdResponse } from '@/apis/section/types'
import {
  generateWeeklyPeerEvaluationAveragesForSection,
  getWeeklyPeerEvaluationsForStudent
} from '@/apis/evaluation'
import type {
  GenerateWeeklyPeerEvaluationAveragesForSectionResponse,
  GetWeeklyPeerEvaluationsForStudentResponse,
  PeerEvaluation,
  PeerEvaluationAverage,
  Rating,
  RatingAverage
} from '@/apis/evaluation/types'
import { getPreviousWeek } from '@/utils/week'
import SearchWeek from '@/components/SearchWeek.vue'
import { ElMessage } from 'element-plus'

const week = ref<string>(getPreviousWeek()) // Week for which to load peer evaluations, default to previous week
const peerEvalutionAverages = ref<PeerEvaluationAverage[]>()
const studentsMissingPeerEvaluations = ref<string[]>([])
const defaultSectionId = ref<number>() // Section ID of the team
const criteria = ref<Criterion[]>([]) // Criteria used for peer evaluations
const loading = ref<boolean>(true)

const settingsStore = useSettingsStore()

// Load data when the component is mounted
onMounted(async () => {
  defaultSectionId.value = settingsStore.defaultSectionId
  if (Number.isNaN(defaultSectionId.value)) {
    ElMessage.error('Please select a section in the Sections page.')
    return
  }

  await loadCriteria()

  loadSectionsPeerEvaluationAverages()
})

// Load criteria information
async function loadCriteria() {
  const sectionResult: FindSectionByIdResponse = await findSectionById(
    defaultSectionId.value as number
  )
  const rubricResult: FindRubricByIdResponse = await findRubricById(
    sectionResult.data.rubricId as number
  )
  criteria.value = rubricResult.data.criteria as Criterion[]
}

async function loadSectionsPeerEvaluationAverages() {
  loading.value = true
  const result: GenerateWeeklyPeerEvaluationAveragesForSectionResponse =
    await generateWeeklyPeerEvaluationAveragesForSection(defaultSectionId.value!, week.value)
  peerEvalutionAverages.value = result.data.peerEvaluationAverages
  // Sort students by last name
  peerEvalutionAverages.value.sort((a, b) => a.lastName.localeCompare(b.lastName))
  studentsMissingPeerEvaluations.value = result.data.studentsMissingPeerEvaluations
  // Convert "first name last name" to "last name, first name" in studentsMissingPeerEvaluations and then sort
  studentsMissingPeerEvaluations.value = studentsMissingPeerEvaluations.value.map((student) => {
    const [firstName, lastName] = student.split(' ')
    return `${lastName}, ${firstName}`
  })
  // Sort students in alphabetical order
  studentsMissingPeerEvaluations.value.sort()
  loading.value = false
}

function resetSearchCriteria() {
  week.value = getPreviousWeek()
  loadSectionsPeerEvaluationAverages()
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailedWeeklyEvaluations = ref<PeerEvaluation[]>([])

async function showDetailedWeeklyEvaluations(peerEvaluationAverage: PeerEvaluationAverage) {
  const result: GetWeeklyPeerEvaluationsForStudentResponse =
    await getWeeklyPeerEvaluationsForStudent(peerEvaluationAverage.studentId, week.value)
  detailedWeeklyEvaluations.value = result.data
  dialogTitle.value = `${peerEvaluationAverage.firstName} ${peerEvaluationAverage.lastName}'s Peer Evaluations`
  dialogVisible.value = true
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
