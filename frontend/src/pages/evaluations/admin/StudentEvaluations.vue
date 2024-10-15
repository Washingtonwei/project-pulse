<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Student Peer Evaluations</span>
      </div>
    </template>
    <!-- Search Form -->
    <SearchPeriod
      v-model:selectedStartWeekNumber="period.startWeek"
      v-model:selectedEndWeekNumber="period.endWeek"
      @search="loadPeerEvaluationAverages"
      @reset="loadPeerEvaluationAverages"
    />
    <el-tabs v-model="activeName" class="demo-tabs">
      <el-tab-pane label="Details" name="details">
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
          <el-table-column label="Week" prop="week"></el-table-column>
          <el-table-column label="Average Total Score" min-width="100px">
            <template #default="{ row }">
              {{ row.averageTotalScore.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column
            v-for="criterion in criteria"
            :key="criterion.criterionId"
            min-width="150px"
          >
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
          <template #empty>
            <el-empty description="No data is available." />
          </template>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="Visualization" name="visualization ">
        <Line :data="evaluationData" :options="lineChartOptions" height="600" width="1500" />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { findSectionById } from '@/apis/section'
import { findRubricById } from '@/apis/rubric'
import type { FindRubricByIdResponse, Criterion } from '@/apis/rubric/types'
import type { FindSectionByIdResponse } from '@/apis/section/types'
import { generateWeeklyPeerEvaluationAveragesForStudentWithinOnePeriod } from '@/apis/evaluation'
import type {
  GeneratePeerEvaluationAveragesForOneStudentResponse,
  PeerEvaluationAverage,
  PeriodParams,
  RatingAverage
} from '@/apis/evaluation/types'
import SearchPeriod from '@/components/SearchPeriod.vue'
import { useSettingsStore } from '@/stores/settings'
import { Line } from 'vue-chartjs'

const { studentId } = defineProps(['studentId'])
const settingsStore = useSettingsStore()
const studentSectionId = settingsStore.defaultSectionId
const peerEvalutionAverages = ref<PeerEvaluationAverage[]>()
const criteria = ref<Criterion[]>([]) // Criteria used for peer evaluations
const loading = ref<boolean>(true)
const period = ref<PeriodParams>({
  startWeek: '',
  endWeek: ''
})

const activeName = ref<string>('details') // The active tab name

// Load data when the component is mounted
onMounted(async () => {
  await loadCriteria()
  loadPeerEvaluationAverages()
})

// Load criteria information
async function loadCriteria() {
  const sectionResult: FindSectionByIdResponse = await findSectionById(studentSectionId)
  const rubricResult: FindRubricByIdResponse = await findRubricById(
    sectionResult.data.rubricId as number
  )
  criteria.value = rubricResult.data.criteria!
}

async function loadPeerEvaluationAverages() {
  loading.value = true
  const result: GeneratePeerEvaluationAveragesForOneStudentResponse =
    await generateWeeklyPeerEvaluationAveragesForStudentWithinOnePeriod(studentId, period.value)
  peerEvalutionAverages.value = result.data
  loading.value = false
}

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

// Computed properties for chart data and options
const evaluationData = computed(() => {
  const weeks = peerEvalutionAverages.value?.map((evaluation) => evaluation.week)
  const evaluationAvgTotalScores = peerEvalutionAverages.value?.map(
    (evaluation) => evaluation.averageTotalScore
  )
  return {
    labels: weeks,
    datasets: [
      {
        label: 'Peer Evaluations',
        data: evaluationAvgTotalScores,
        backgroundColor: ['#66b1ff']
      }
    ]
  }
})

const lineChartOptions = {
  responsive: false,
  maintainAspectRatio: false,
  scales: {
    x: {
      title: {
        display: true,
        text: 'Week'
      }
    },
    y: {
      title: {
        display: true,
        text: 'Average Total Score'
      }
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
