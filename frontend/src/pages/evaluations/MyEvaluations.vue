<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>My Peer Evaluations</span>
      </div>
    </template>
    <!-- Search Form -->
    <SearchPeriod
      v-model:selectedStartWeekNumber="period.startWeek"
      v-model:selectedEndWeekNumber="period.endWeek"
      @search="loadPeerEvaluationAverages"
      @reset="loadPeerEvaluationAverages"
    />
    <!-- Evaluation Table -->
    <el-table
      :data="peerEvalutionAverages"
      style="width: 100%"
      stripe
      border
      v-loading="loading"
      height="600"
    >
      <el-table-column label="Week" prop="week"></el-table-column>
      <el-table-column label="Average Total Score" width="100px">
        <template #default="{ row }">
          {{ row.averageTotalScore.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column v-for="criterion in criteria" :key="criterion.criterionId">
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
      <el-table-column label="Public Comments" width="300px">
        <template #default="{ row }">
          <p v-for="(comment, index) in row.publicComments" :key="index">
            {{ comment }}
          </p>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="No data is available." />
      </template>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { Student } from '@/apis/student/types'
import { findSectionById } from '@/apis/section'
import { findRubricById } from '@/apis/rubric'
import type { FindRubricByIdResponse, Criterion } from '@/apis/rubric/types'
import { useUserInfoStore } from '@/stores/userInfo'
import type { FindSectionByIdResponse } from '@/apis/section/types'
import { generateWeeklyPeerEvaluationAveragesForStudentWithinOnePeriod } from '@/apis/evaluation'
import type {
  GeneratePeerEvaluationAveragesForOneStudentResponse,
  PeerEvaluationAverage,
  PeriodParams,
  RatingAverage
} from '@/apis/evaluation/types'
import SearchPeriod from '@/components/SearchPeriod.vue'
import { ElMessage } from 'element-plus'

const peerEvalutionAverages = ref<PeerEvaluationAverage[]>()
const sectionId = ref<number>() // Section ID of the team
const criteria = ref<Criterion[]>([]) // Criteria used for peer evaluations

const loading = ref<boolean>(false)

const period = ref<PeriodParams>({
  startWeek: '',
  endWeek: ''
})

const userInfoStore = useUserInfoStore() // Need team ID to search for students

// Load data when the component is mounted
onMounted(async () => {
  sectionId.value = (userInfoStore.userInfo as Student).sectionId as number
  await loadCriteria()
  if (!(userInfoStore.userInfo as Student).teamId) {
    ElMessage.error('You have not been assigned a team yet.')
    return
  }

  loadPeerEvaluationAverages()
})

// Load criteria information
async function loadCriteria() {
  const sectionResult: FindSectionByIdResponse = await findSectionById(sectionId.value as number)
  const rubricResult: FindRubricByIdResponse = await findRubricById(
    sectionResult.data.rubricId as number
  )
  criteria.value = rubricResult.data.criteria!
}

async function loadPeerEvaluationAverages() {
  loading.value = true
  const result: GeneratePeerEvaluationAveragesForOneStudentResponse =
    await generateWeeklyPeerEvaluationAveragesForStudentWithinOnePeriod(
      (userInfoStore.userInfo as Student).id as number,
      period.value
    )
  peerEvalutionAverages.value = result.data
  loading.value = false
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
