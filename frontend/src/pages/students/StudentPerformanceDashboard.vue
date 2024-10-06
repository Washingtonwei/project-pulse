<template>
  <el-text class="mx-1" size="large">{{ studentName }}</el-text>
  <StudentActivities :studentId="studentId" />
  <el-divider />
  <StudentEvaluations :studentId="studentId" />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import StudentActivities from '../activities/admin/StudentActivities.vue'
import StudentEvaluations from '../evaluations/admin/StudentEvaluations.vue'

import { useRoute } from 'vue-router'
import { findStudentById } from '@/apis/student'
import type { FindStudentByIdResponse } from '@/apis/student/types'

const route = useRoute()

const studentId: number = Number(route.params.studentId)
const studentName = ref<string>('')

onMounted(async () => {
  const result: FindStudentByIdResponse = await findStudentById(studentId)
  const student = result.data
  studentName.value = student.firstName + ' ' + student.lastName
})
</script>

<style scoped></style>
