<template>
  <!-- Form for editing an activity -->
  <el-form ref="activityForm" :model="activityBeingCommented" label-width="100px">
    <el-form-item label="Category:" prop="category">
      <el-input v-model="activityBeingCommented.category" disabled></el-input>
    </el-form-item>
    <el-form-item label="Activity:" prop="activity">
      <el-input v-model="activityBeingCommented.activity" disabled></el-input>
    </el-form-item>
    <el-form-item label="Description:" prop="description">
      <el-input type="textarea" v-model="activityBeingCommented.description" disabled></el-input>
    </el-form-item>
    <el-form-item label="Planned Hours:" prop="plannedHours">
      <el-input v-model="activityBeingCommented.plannedHours" disabled></el-input>
    </el-form-item>
    <el-form-item label="Actual Hours:" prop="actualHours">
      <el-input v-model="activityBeingCommented.actualHours" disabled></el-input>
    </el-form-item>
    <el-form-item label="Status:" prop="status">
      <el-input v-model="activityBeingCommented.status" disabled></el-input>
    </el-form-item>
    <el-form-item label="Comment:" prop="status">
      <el-input v-model="comment" max-length="255" show-word-limit></el-input>
    </el-form-item>
    <div style="display: flex; justify-content: end">
      <el-form-item>
        <el-button @click="$emit('close-drawer')"> Cancel </el-button>
        <el-button type="primary" @click="commentExistingActivity()"> Confirm </el-button>
      </el-form-item>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { ActivityCategory, ActivityStatus, type Activity } from '@/apis/activity/types'
import { findActivityById, commentActivity } from '@/apis/activity'
import { ref, onMounted } from 'vue'

const { activityId } = defineProps(['activityId']) // Get the activity ID from the parent component
const emit = defineEmits(['close-drawer'])

const activityBeingCommented = ref<Activity>({
  week: '',
  category: ActivityCategory.DEVELOPMENT,
  activity: '',
  description: '',
  plannedHours: 0,
  actualHours: 0,
  status: ActivityStatus.IN_PROGRESS
})

const comment = ref<string>('') // Comment to be added to the activity

function clearForm() {
  activityBeingCommented.value = {
    week: '',
    category: ActivityCategory.DEVELOPMENT,
    activity: '',
    description: '',
    plannedHours: 0,
    actualHours: 0,
    status: ActivityStatus.IN_PROGRESS
  }
  comment.value = ''
}

async function loadActivity(id: number) {
  // Load the activity to be commented
  const result = await findActivityById(id)
  activityBeingCommented.value = result.data // Set the activity to be commented
}

// Since this form is in a drawer, we need to turn on the destroyOnClose property of the drawer to ensure that the form is re-mounted every time the drawer is opened
onMounted(() => {
  clearForm() // Clear the form
  loadActivity(activityId) // Load the activity to be commented
})

async function commentExistingActivity() {
  // Call the API to update the activity
  await commentActivity(activityId, comment.value)

  ElMessage.success('Activity commented successfully')

  emit('close-drawer')
}
</script>

<style lang="scss" scoped></style>
