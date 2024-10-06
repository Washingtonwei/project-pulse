<template>
  <div class="forgot-password">
    <el-card class="box-card">
      <h2>Forgot your password?</h2>
      <el-form
        @submit.prevent="submitEmail"
        label-position="top"
        :model="emailForm"
        ref="emailFormRef"
        :rules="rules"
      >
        <el-form-item label="Email" prop="email">
          <el-input
            v-model="emailForm.email"
            placeholder="Enter your email"
            type="email"
            clearable
          />
        </el-form-item>
        <el-button type="primary" @click="submitEmail" :loading="loading">
          Send Reset Password Link
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useRouter } from 'vue-router'
import { sendResetPasswordLink } from '@/apis/login'

// State management
const emailForm = ref({
  email: ''
})

const loading = ref<boolean>(false)

const emailFormRef = ref<FormInstance>()

const rules = {
  email: [
    { required: true, message: 'Please input your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email address', trigger: ['blur', 'change'] }
  ]
}

// Router instance
const router = useRouter()

// Submit email function
const submitEmail = async () => {
  await emailFormRef.value!.validate() // Validate the form

  loading.value = true

  await sendResetPasswordLink(emailForm.value.email)
  ElMessage.success('A password reset link has been sent to your email.')
  router.push('/login')
}
</script>

<style scoped>
.forgot-password {
  max-width: 400px;
  margin: 0 auto;
  padding-top: 100px;
}
</style>
