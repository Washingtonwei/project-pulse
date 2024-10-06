<template>
  <!-- Form for editing an activity -->
  <el-form ref="activityForm" :model="activityBeingEdited" :rules="rules" label-width="100px">
    <el-form-item label="Category:" prop="category">
      <el-select v-model="activityBeingEdited.category" placeholder="Select a category" clearable>
        <el-option
          v-for="category in categories"
          :key="category"
          :label="category"
          :value="category"
        ></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Activity:" prop="activity">
      <el-input
        v-model="activityBeingEdited.activity"
        placeholder="Type in activity name"
      ></el-input>
    </el-form-item>
    <el-form-item label="Description:" prop="description">
      <el-input
        type="textarea"
        v-model="activityBeingEdited.description"
        placeholder="Type in activity description"
      ></el-input>
    </el-form-item>
    <el-form-item label="Planned Hrs:" prop="plannedHours">
      <el-input
        v-model="activityBeingEdited.plannedHours"
        placeholder="Type in planned hours"
      ></el-input>
    </el-form-item>
    <el-form-item label="Actual Hrs:" prop="actualHours">
      <el-input
        v-model="activityBeingEdited.actualHours"
        placeholder="Type in actual hours"
      ></el-input>
    </el-form-item>
    <el-form-item label="Status:" prop="status">
      <el-select v-model="activityBeingEdited.status" placeholder="Select a status" clearable>
        <el-option
          v-for="status in statuses"
          :key="status"
          :label="status"
          :value="status"
        ></el-option>
      </el-select>
    </el-form-item>
    <div style="display: flex; justify-content: end">
      <el-form-item>
        <el-button @click="$emit('close-drawer')"> Cancel </el-button>
        <el-button type="primary" @click="updateExistingActivity()"> Confirm </el-button>
      </el-form-item>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { Activity } from '@/apis/activity/types'
import { ActivityCategory, ActivityStatus } from '@/apis/activity/types'
import type { FormInstance } from 'element-plus'
import { findActivityById, updateActivity } from '@/apis/activity'
import { ref, onMounted } from 'vue'

const { activityId } = defineProps(['activityId'])
const emit = defineEmits(['close-drawer'])

const activityBeingEdited = ref<Activity>({
  week: '',
  category: ActivityCategory.DEVELOPMENT,
  activity: '',
  description: '',
  plannedHours: 0,
  actualHours: 0,
  status: ActivityStatus.IN_PROGRESS
})

const categories = ref<string[]>(Object.values(ActivityCategory)) // Activity categories
const statuses = ref<string[]>(Object.values(ActivityStatus)) // Activity statuses

const activityForm = ref<FormInstance>() // Form reference

// Validation rules
const rules = {
  category: [{ required: true, message: 'Please select a category', trigger: 'blur' }],
  activity: [{ required: true, message: 'Please type in an activity name', trigger: 'blur' }],
  description: [{ required: true, message: 'Please type in a description', trigger: 'blur' }],
  plannedHours: [{ required: true, message: 'Please type in planned hours', trigger: 'blur' }],
  actualHours: [{ required: true, message: 'Please type in actual hours', trigger: 'blur' }],
  status: [{ required: true, message: 'Please select a status', trigger: 'blur' }]
}

function clearForm() {
  activityBeingEdited.value = {
    week: '',
    category: ActivityCategory.DEVELOPMENT,
    activity: '',
    description: '',
    plannedHours: 0,
    actualHours: 0,
    status: ActivityStatus.IN_PROGRESS
  }
}

async function loadActivity(id: number) {
  // Load the activity to be edited
  const result = await findActivityById(id)
  activityBeingEdited.value = result.data // Set the activity to be edited
}

// Since this form is in a drawer, we need to turn on the destroyOnClose property of the drawer to ensure that the form is re-mounted every time the drawer is opened
onMounted(() => {
  clearForm() // Clear the form
  loadActivity(activityId) // Load the activity to be edited
})

async function updateExistingActivity() {
  await activityForm.value!.validate() // Validate the form

  const updatedActivity: Activity = {
    activityId: activityBeingEdited.value.activityId, // No change
    week: activityBeingEdited.value.week, // No change
    category: activityBeingEdited.value.category,
    activity: activityBeingEdited.value.activity,
    description: activityBeingEdited.value.description,
    plannedHours: activityBeingEdited.value.plannedHours,
    actualHours: activityBeingEdited.value.actualHours,
    status: activityBeingEdited.value.status
  }

  // Call the API to update the activity
  await updateActivity(updatedActivity)

  ElMessage.success('Activity updated successfully')

  emit('close-drawer')
}
</script>

<style lang="scss" scoped></style>
