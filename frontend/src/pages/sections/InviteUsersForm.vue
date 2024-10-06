<template>
  <el-form label-width="110px" style="padding-right: 30px" label-position="left">
    <el-form-item label="Email:" prop="email">
      <input name="input" ref="emailInputBox" />
    </el-form-item>
  </el-form>
  <div style="display: flex; justify-content: end">
    <el-form-item>
      <el-button @click="$emit('close-dialog')"> Cancel </el-button>
      <el-button type="primary" @click="inviteStudents()"> Confirm </el-button>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import Tagify from '@yaireo/tagify'
import '@yaireo/tagify/dist/tagify.css' // Don't forget to import the CSS file
import { sendEmailInvitationsToStudents } from '@/apis/section'
import { ElMessage } from 'element-plus'

const { courseId, sectionId } = defineProps(['courseId', 'sectionId'])
const emit = defineEmits(['close-dialog'])
const studentEmails = ref<string[]>([])
const emailInputBox = ref<HTMLInputElement | null>(null)
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
  studentEmails.value?.push(event.detail.data.value)
}

// remove email from the model
function removeEmail(event: any) {
  studentEmails.value = studentEmails.value.filter((email) => email !== event.detail.data.value)
}

async function inviteStudents() {
  // Call the API to invite students
  sendEmailInvitationsToStudents(courseId, sectionId, studentEmails.value)
  ElMessage.success('Students invited successfully')

  // Close the dialog
  emit('close-dialog')
}
</script>

<style lang="scss" scoped></style>
