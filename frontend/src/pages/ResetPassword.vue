<template>
  <div class="reset-password">
    <el-card class="box-card">
      <h2>Reset Your Password</h2>
      <el-form
        @submit.prevent="resetPassword"
        label-position="top"
        :model="passwordForm"
        ref="passwordFormRef"
        :rules="rules"
      >
        <!-- New password field -->
        <el-form-item label="New Password" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            placeholder="Enter new password"
            show-password
            required
          />
        </el-form-item>

        <!-- Confirm password field -->
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="Confirm new password"
            show-password
            required
          />
        </el-form-item>

        <!-- Submit button -->
        <el-button type="primary" @click="resetPassword" :loading="loading">Submit</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { resetUserPassword } from '@/apis/login'

// State management
const passwordForm = ref({
  password: '',
  confirmPassword: ''
})

const rules = {
  // Password rule: required, min length 6, contains at least one letter and one number
  password: [
    { required: true, message: 'Please input your password', trigger: 'blur' },
    {
      min: 6,
      message: 'Password must be at least 6 characters long',
      trigger: 'blur'
    },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/,
      message: 'Password must contain at least one letter and one number',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.value.password) {
          callback(new Error('Passwords do not match'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const resetPasswordEmail = ref<string>('')
const resetPasswordToken = ref<string>('')

// Form reference for validation (optional, can be used to reset form)
const passwordFormRef = ref<FormInstance>()

const loading = ref<boolean>(false)

// Router for navigation and route for capturing query params
const route = useRoute()
const router = useRouter()

onMounted(() => {
  const { email, token } = route.query as Record<string, string> // Get email and token from query params
  if (!email || !token) {
    ElMessage.error('Invalid password reset link')
    router.push('/403')
  } else {
    resetPasswordEmail.value = email
    resetPasswordToken.value = token
  }
})

// Reset password function
const resetPassword = async () => {
  await passwordFormRef.value!.validate() // Validate the form

  loading.value = true

  await resetUserPassword(
    resetPasswordEmail.value,
    resetPasswordToken.value,
    passwordForm.value.password
  )
  ElMessage.success('Password reset successfully')
  router.push('/login')
}
</script>

<style scoped>
.reset-password {
  max-width: 400px;
  margin: 0 auto;
  padding-top: 100px;
}

.box-card {
  padding: 20px;
}

.el-alert {
  margin-top: 20px;
}
</style>
