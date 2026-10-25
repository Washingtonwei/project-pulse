<template>
  <!-- UC-NOT-remind-non-submitters: choose an item, review who owes it, confirm -->
  <el-dialog
    v-model="visible"
    title="Remind students who have not submitted"
    width="600px"
    data-cy="remind-dialog"
    @open="reset"
  >
    <el-form label-position="top">
      <el-form-item label="Which item?">
        <el-radio-group v-model="item" :disabled="sending || result !== null" @change="loadStatus">
          <el-radio-button value="WEEKLY_ACTIVITY_REPORT" data-cy="remind-item-war">
            Weekly activity report
          </el-radio-button>
          <el-radio-button value="PEER_EVALUATION" data-cy="remind-item-peer-evaluation">
            Peer evaluation
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <div v-loading="loading">
      <template v-if="status">
        <p data-cy="remind-week">
          Week of {{ status.weekRange }}<span v-if="dueLabel">, due {{ dueLabel }}</span>
        </p>

        <!-- 4a -->
        <el-alert
          v-if="!status.weekActive"
          type="info"
          :closable="false"
          show-icon
          title="Reminders are sent only for an active week."
          data-cy="remind-inactive-week"
        />
        <!-- 4b -->
        <el-alert
          v-else-if="status.teams.length === 0"
          type="success"
          :closable="false"
          show-icon
          title="Every student who can submit it has submitted it."
          data-cy="remind-nobody-owes"
        />
        <!-- Step 4: the students who owe the item, by team -->
        <div v-else-if="result === null" data-cy="remind-owing">
          <p>These students will be reminded:</p>
          <div v-for="team in status.teams" :key="team.teamId" class="team">
            <strong>{{ team.teamName }}</strong>
            <div class="students">
              <el-tag v-for="student in team.students" :key="student.id" type="danger">
                {{ student.firstName }} {{ student.lastName }}
              </el-tag>
            </div>
          </div>
        </div>
      </template>

      <!-- Step 7, and 6a2 -->
      <template v-if="result">
        <el-alert
          :type="result.failed.length > 0 ? 'warning' : 'success'"
          :closable="false"
          show-icon
          :title="resultTitle"
          data-cy="remind-result"
        />
        <div v-if="result.failed.length > 0" class="failed" data-cy="remind-failed">
          Could not reach:
          <el-tag v-for="student in result.failed" :key="student.id" type="warning">
            {{ student.firstName }} {{ student.lastName }}
          </el-tag>
        </div>
      </template>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false" data-cy="remind-cancel">
          {{ result ? 'Close' : 'Cancel' }}
        </el-button>
        <el-button
          v-if="canConfirm"
          type="primary"
          :loading="sending"
          :disabled="sending"
          @click="confirm"
          data-cy="remind-confirm"
        >
          Send reminders
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import moment from 'moment'
import { getSubmissionStatus, sendReminders } from '@/apis/notification'
import type { ReminderResult, SubmissionStatus, SubmittableItem } from '@/apis/notification/types'

const props = defineProps<{
  sectionId: number
}>()

const visible = defineModel<boolean>({ required: true })

const item = ref<SubmittableItem>()
const status = ref<SubmissionStatus | null>(null)
const result = ref<ReminderResult | null>(null)
const loading = ref(false)
const sending = ref(false)

// Step 2: every opening starts from the choice of item
function reset() {
  latestStatusRequest++
  item.value = undefined
  status.value = null
  result.value = null
  loading.value = false
  sending.value = false
}

// Step 4: the week is always the previous week, which the server computes. Only the latest request may fill the
// list, so switching items quickly cannot leave one item's students on screen while Confirm sends the other.
let latestStatusRequest = 0
async function loadStatus() {
  if (!item.value) return
  const request = ++latestStatusRequest
  loading.value = true
  status.value = null
  try {
    const response = await getSubmissionStatus(props.sectionId, item.value)
    if (request === latestStatusRequest) status.value = response.data
  } finally {
    if (request === latestStatusRequest) loading.value = false
  }
}

const canConfirm = computed(
  () =>
    status.value !== null &&
    status.value.weekActive &&
    status.value.teams.length > 0 &&
    result.value === null
)

// Steps 5 to 7. Confirm stays disabled while the request is in flight, so a double click sends once. The request
// names the week on screen; if the server's previous week has moved on (Confirm after Monday midnight), it refuses,
// and the list is reloaded for the new week so the instructor reviews it before sending.
async function confirm() {
  if (!item.value || !status.value || sending.value) return
  sending.value = true
  try {
    const response = await sendReminders(props.sectionId, item.value, status.value.week)
    result.value = response.data
  } catch {
    await loadStatus()
  } finally {
    sending.value = false
  }
}

const dueLabel = computed(() => {
  if (!status.value?.dueDay) return ''
  const day = status.value.dueDay.charAt(0) + status.value.dueDay.slice(1).toLowerCase()
  return status.value.dueTime
    ? `${day} at ${moment(status.value.dueTime, 'HH:mm').format('h:mm A')}`
    : day
})

const resultTitle = computed(() => {
  if (!result.value) return ''
  // Who is emailed is decided at send time, so everyone listed may have submitted since
  if (result.value.sent === 0 && result.value.failed.length === 0) {
    return 'No reminders were needed: every listed student has submitted since.'
  }
  return `${result.value.sent} ${result.value.sent === 1 ? 'reminder' : 'reminders'} sent.`
})
</script>

<style lang="scss" scoped>
.team {
  margin-bottom: 10px;
}
.students,
.failed {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 5px;
}
.failed {
  align-items: center;
  margin-top: 10px;
}
</style>
