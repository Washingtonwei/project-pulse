<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>User Profile</span>
      </div>
    </template>
    <el-row>
      <el-col :span="12">
        <el-form ref="userForm" :model="userInfo" :rules="rules" label-width="100px" size="large">
          <el-form-item label="Id">
            <el-input v-model="userInfo.id" disabled></el-input>
          </el-form-item>
          <el-form-item label="Username" prop="username">
            <el-input v-model="userInfo.username"></el-input>
          </el-form-item>
          <el-form-item label="First Name" prop="firstName">
            <el-input v-model="userInfo.firstName"></el-input>
          </el-form-item>
          <el-form-item label="Last Name" prop="lastName">
            <el-input v-model="userInfo.lastName"></el-input>
          </el-form-item>
          <el-form-item label="Email" prop="email">
            <el-input v-model="userInfo.email"></el-input>
          </el-form-item>
          <el-form-item label="Status">
            <el-input :value="userInfo.enabled ? 'Enabled' : 'Disabled'" disabled></el-input>
          </el-form-item>
          <el-form-item label="Roles">
            <el-tag
              v-for="role in userInfo.roles!.split(' ')"
              :key="role"
              type="info"
              style="margin-right: 5px"
            >
              {{ role }}
            </el-tag>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateCurrentUser">Update</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </el-card>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import { updateStudent } from '@/apis/student'
import type { Student } from '@/apis/student/types'
import type { Instructor } from '@/apis/instructor/types'

const userInfoStore = useUserInfoStore()

const userInfo = ref<Student | Instructor>({
  id: userInfoStore.userInfo!.id,
  username: userInfoStore.userInfo!.username,
  firstName: userInfoStore.userInfo!.firstName,
  lastName: userInfoStore.userInfo!.lastName,
  email: userInfoStore.userInfo!.email,
  enabled: userInfoStore.userInfo!.enabled,
  roles: userInfoStore.userInfo!.roles
})

const userForm = ref()

const rules = {
  username: [
    { required: true, message: 'Username cannot be empty.', trigger: 'blur' },
    {
      pattern: /^\S{2,10}$/,
      message: 'Username must be between 2 to 10 characters long.',
      trigger: 'blur'
    }
  ],
  firstName: [{ required: true, message: 'First name cannot be empty.', trigger: 'blur' }],
  lastName: [{ required: true, message: 'Last name cannot be empty.', trigger: 'blur' }],
  email: [
    { required: true, message: 'Email cannot be empty.', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format.', trigger: 'blur' }
  ]
}

async function updateCurrentUser() {
  await userForm.value.validate()

  // Call the API to update the user
  await updateStudent(userInfo.value)
  ElMessage.success('User updated successfully')
  // Update the user info in the store
  userInfoStore.setUserInfo(userInfo.value)
}
</script>
<style lang="scss" scoped></style>
