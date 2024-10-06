<template>
  <!-- Form for adding a new activity -->
  <el-form ref="activityForm" :model="newActivity" :rules="rules" label-width="100px">
    <el-form-item label="Week:" prop="week">
      <el-input v-model="newActivity.week" disabled></el-input>
    </el-form-item>
    <el-form-item label="Category:" prop="category">
      <el-select v-model="newActivity.category" placeholder="Select a category" clearable>
        <el-option
          v-for="category in categories"
          :key="category"
          :label="category"
          :value="category"
        ></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Activity:" prop="activity">
      <el-input v-model="newActivity.activity" placeholder="Type in activity name"></el-input>
    </el-form-item>
    <el-form-item label="Description:" prop="description">
      <el-input
        type="textarea"
        v-model="newActivity.description"
        autosize
        placeholder="Type in activity description"
        maxlength="255"
        show-word-limit
      ></el-input>
    </el-form-item>
    <el-form-item label="Planned Hrs:" prop="plannedHours">
      <el-input v-model="newActivity.plannedHours" placeholder="Type in planned hours"></el-input>
    </el-form-item>
    <el-form-item label="Actual Hrs:" prop="actualHours">
      <el-input v-model="newActivity.actualHours" placeholder="Type in actual hours"></el-input>
    </el-form-item>
    <el-form-item label="Status:" prop="status">
      <el-select v-model="newActivity.status" placeholder="Select a status" clearable>
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
        <el-button type="primary" @click="addActivity()" :loading="isLoading"> Confirm </el-button>
      </el-form-item>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { Activity } from '@/apis/activity/types'
import { ActivityCategory, ActivityStatus } from '@/apis/activity/types'
import type { FormInstance } from 'element-plus'
import { createActivity } from '@/apis/activity'
import { ref, onMounted } from 'vue'

const { week } = defineProps(['week']) // Week number
const emit = defineEmits(['close-drawer'])

const newActivity = ref<Activity>({
  week: week, // Week number is passed in from the MyActivities component
  category: ActivityCategory.DEPLOYMENT,
  activity: '',
  description: '',
  plannedHours: 0,
  actualHours: 0,
  status: ActivityStatus.IN_PROGRESS
})

const categories = ref<string[]>(Object.values(ActivityCategory)) // Activity categories
const statuses = ref<string[]>(Object.values(ActivityStatus)) // Activity statuses
// loading state
const isLoading = ref(false)

const activityForm = ref<FormInstance>() // Form reference

// Validation rules
const rules = {
  category: [{ required: true, message: 'Please select a category', trigger: 'blur' }],
  activity: [{ required: true, message: 'Please type in an activity name', trigger: 'blur' }],
  description: [
    { required: true, message: 'Please type in a description', trigger: 'blur' },
    // VARCHAR(255) constraint
    { max: 255, message: 'Description length should be less than 255 characters', trigger: 'blur' }
  ],
  plannedHours: [{ required: true, message: 'Please type in planned hours', trigger: 'blur' }],
  actualHours: [{ required: true, message: 'Please type in actual hours', trigger: 'blur' }],
  status: [{ required: true, message: 'Please select a status', trigger: 'blur' }]
}

function clearForm() {
  newActivity.value = {
    week: week,
    category: ActivityCategory.DEPLOYMENT,
    activity: '',
    description: '',
    plannedHours: 0,
    actualHours: 0,
    status: ActivityStatus.IN_PROGRESS
  }
}

onMounted(() => {
  clearForm()
})

async function addActivity() {
  await activityForm.value!.validate() // Validate the form
  isLoading.value = true
  // Call the API to create a new activity
  await createActivity(newActivity.value)
  ElMessage.success('Activity added successfully')
  isLoading.value = false
  emit('close-drawer')
}
</script>

<style lang="scss" scoped></style>
