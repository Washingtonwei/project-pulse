<template>
  <el-form label-width="110px" style="padding-right: 30px" label-position="left">
    <el-form-item label="Role:" prop="role">
      <el-select v-model="selectedRole" placeholder="Select Role" style="width: 100%">
        <el-option label="Student" value="student" />
        <el-option label="Instructor" value="instructor" />
      </el-select>
    </el-form-item>
    <el-form-item label="Email:" prop="email">
      <input name="input" ref="emailInputBox" />
    </el-form-item>
  </el-form>
  <div style="display: flex; justify-content: end">
    <el-form-item>
      <el-button @click="$emit('close-dialog')"> Cancel </el-button>
      <el-button type="primary" @click="inviteUsers()"> Confirm </el-button>
    </el-form-item>
  </div>

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

const { courseId, sectionId } = defineProps(['courseId', 'sectionId'])
const emit = defineEmits(['close-dialog'])
const userEmails = ref<string[]>([])
const emailInputBox = ref<HTMLInputElement | null>(null)
const selectedRole = ref<string>('student')
const showSummary = ref<boolean>(false)
const summaryData = ref<any>(null)
let tagify: Tagify | null = null

onMounted(() => {
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
})

// add email to the model
function addEmail(event: any) {
  userEmails.value?.push(event.detail.data.value)
}

// remove email from the model
function removeEmail(event: any) {
  userEmails.value = userEmails.value.filter((email) => email !== event.detail.data.value)
}

async function inviteUsers() {
  try {
    if (selectedRole.value === 'student') {
      // Call the API to invite students
      await sendEmailInvitationsToStudents(courseId, sectionId, userEmails.value)
      ElMessage.success('Students invited successfully')
      // Close the dialog
      emit('close-dialog')
    } else {
      // Call the API to invite or add instructors
      const response = await inviteOrAddInstructors(courseId, sectionId, userEmails.value)
      summaryData.value = response.data
      showSummary.value = true
    }
  } catch (error) {
    ElMessage.error('Failed to process invitation')
  }
}

function closeSummary() {
  showSummary.value = false
  emit('close-dialog')
}
</script>

<style lang="scss" scoped></style>
