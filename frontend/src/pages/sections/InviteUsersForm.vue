<template>
  <el-form label-width="110px" style="padding-right: 30px" label-position="left">
    <!-- Email input with Tagify and helper text -->
    <el-form-item label="Email(s):" prop="email">
      <input name="input" ref="emailInputBox" />
      <el-text size="small" type="info"> Separate multiple emails with semicolons. </el-text>
    </el-form-item>

    <!-- Invite role -->
    <el-form-item label="Invite as:" prop="role">
      <el-radio-group v-model="inviteRole">
        <el-radio label="student">Student</el-radio>
        <el-radio label="instructor">Instructor</el-radio>
      </el-radio-group>
    </el-form-item>

    <!-- Actions -->
    <div style="display: flex; justify-content: end">
      <el-form-item>
        <el-button @click="$emit('close-dialog')"> Cancel </el-button>
        <el-button type="primary" @click="sendInvitations" :loading="submitting">
          Confirm
        </el-button>
      </el-form-item>
    </div>
  </el-form>

  <!-- Summary Dialog -->
  <el-dialog v-model="showSummary" title="Invitation Summary" width="500px">
    <div v-if="summaryData">
      <div v-if="summaryData.added && summaryData.added.length > 0">
        <h4>Added (existing users):</h4>
        <ul>
          <li v-for="email in summaryData.added" :key="email">{{ email }}</li>
        </ul>
      </div>
      <div v-if="summaryData.invited && summaryData.invited.length > 0">
        <h4>Invited (new users):</h4>
        <ul>
          <li v-for="email in summaryData.invited" :key="email">{{ email }}</li>
        </ul>
      </div>
      <div v-if="summaryData.alreadyExists && summaryData.alreadyExists.length > 0">
        <h4>Already exists:</h4>
        <ul>
          <li v-for="email in summaryData.alreadyExists" :key="email">{{ email }}</li>
        </ul>
      </div>
    </div>
    <template #footer>
      <el-button type="primary" @click="closeSummary()">OK</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import Tagify from '@yaireo/tagify'
import '@yaireo/tagify/dist/tagify.css' // Don't forget to import the CSS file
import { sendEmailInvitationsToStudents, inviteOrAddInstructors } from '@/apis/section'
import { ElMessage } from 'element-plus'
import type { InviteOrAddInstructorsResponse } from '@/apis/section/types'

const { courseId, sectionId } = defineProps<{
  courseId: number
  sectionId: number
}>()

const emit = defineEmits(['close-dialog'])
const invitationEmails = ref<string[]>([])
const emailInputBox = ref<HTMLInputElement | null>(null)
const inviteRole = ref<'student' | 'instructor'>('student')
const submitting = ref(false) // To prevent multiple submissions
const showSummary = ref<boolean>(false)
const summaryData = ref<any>(null)

let tagify: Tagify | null = null

onMounted(async () => {
  emailInputBox.value?.focus() // Not working
  // Initialize Tagify, split tags by semicolon
  tagify = new Tagify(emailInputBox.value!, {
    delimiters: ';',
    callbacks: {
      add: addEmail,
      remove: removeEmail
    }
  })
})

// Clean up when the component is unmounted
onUnmounted(() => {
  tagify?.removeAllTags()
  tagify?.destroy()
  tagify = null
})

// add email to the model
function addEmail(event: any) {
  invitationEmails.value.push(event.detail.data.value)
}

// remove email from the model
function removeEmail(event: any) {
  invitationEmails.value = invitationEmails.value.filter(
    (email) => email !== event.detail.data.value
  )
}

async function sendInvitations() {
  if (invitationEmails.value.length === 0) {
    ElMessage.error('Please enter at least one email address')
    return
  }

  submitting.value = true

  try {
    if (inviteRole.value === 'student') {
      await sendEmailInvitationsToStudents(courseId, sectionId, invitationEmails.value)
      ElMessage.success('Students invited successfully')
    } else {
      const response: InviteOrAddInstructorsResponse = await inviteOrAddInstructors(
        courseId,
        sectionId,
        invitationEmails.value
      )
      summaryData.value = response.data
      showSummary.value = true
    }
  } catch (error) {
    ElMessage.error('Failed to send invitations. Please try again.')
  } finally {
    submitting.value = false
  }
}

// Close the dialog
function closeSummary() {
  showSummary.value = false
  emit('close-dialog')
}
</script>

<style lang="scss" scoped></style>
