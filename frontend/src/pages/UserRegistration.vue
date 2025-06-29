<template>
  <div class="registration-form-container">
    <el-form
      :model="registration"
      :rules="rules"
      ref="registrationForm"
      label-width="150px"
      class="registration-form"
    >
      <el-form-item label="Username" prop="username">
        <el-input
          v-model="registration.username"
          placeholder="Enter your email as username"
        ></el-input>
      </el-form-item>
      <el-form-item label="First Name" prop="firstName">
        <el-input v-model="registration.firstName" placeholder="Enter your first name"></el-input>
      </el-form-item>
      <el-form-item label="Last Name" prop="lastName">
        <el-input v-model="registration.lastName" placeholder="Enter your last name"></el-input>
      </el-form-item>
      <el-form-item label="Email" prop="email">
        <el-input v-model="registration.email" placeholder="Enter your email" disabled></el-input>
      </el-form-item>
      <el-form-item label="Password" prop="password">
        <el-input
          type="password"
          v-model="registration.password"
          placeholder="Enter your password, don't use your TCU password!!!"
        ></el-input>
      </el-form-item>

      <el-form-item label="Confirm Password" prop="confirmPassword">
        <el-input
          type="password"
          v-model="registration.confirmPassword"
          placeholder="Confirm your password"
        ></el-input>
      </el-form-item>

      <el-form-item>
        <el-button @click="reset">Reset</el-button>
        <el-button type="primary" @click="register">Register</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createStudent } from '@/apis/student'
import { createInstructor } from '@/apis/instructor'
import { ElMessage, type FormInstance } from 'element-plus'

interface FormState {
  email: string
  firstName: string
  lastName: string
  username: string
  password: string
  confirmPassword: string
}

const registrationForm = ref<FormInstance>()
const route = useRoute()

const registration = ref<FormState>({
  email: '',
  firstName: '',
  lastName: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const registrationCourseId = ref<number>(NaN)
const registrationSectionId = ref<number>(NaN)
const registrationToken = ref<string>('')
const registrationRole = ref<string>('')

const rules = {
  username: [
    { required: true, message: 'Please input your email as your username', trigger: 'blur' },
    {
      pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
      message: 'Enter your email as username.',
      trigger: 'blur'
    }
  ],
  firstName: [{ required: true, message: 'Please input your first name', trigger: 'blur' }],
  lastName: [{ required: true, message: 'Please input your last name', trigger: 'blur' }],
  email: [{ required: true, message: 'Please input your email', trigger: 'blur' }],

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
      message: 'Password must contain at least one letter and one number, no special characters',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== registration.value.password) {
          callback(new Error('Passwords do not match'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const router = useRouter()

onMounted(() => {
  const { email, token, courseId, sectionId, role } = route.query as Record<string, string> // Destructure query parameters
  if (!email || !token || !role) {
    ElMessage.error('Invalid registration link')
    // redirect to 403
    router.push('/403')
  } else {
    registration.value.email = email
    registrationToken.value = token
    registrationCourseId.value = parseInt(courseId)
    registrationSectionId.value = parseInt(sectionId)
    registrationRole.value = role
  }
})

async function register() {
  await registrationForm.value?.validate()

  if (registrationRole.value === 'student') {
    await createStudent(registration.value, {
      courseId: registrationCourseId.value,
      sectionId: registrationSectionId.value,
      registrationToken: registrationToken.value,
      role: registrationRole.value
    })
  } else {
    await createInstructor(registration.value, {
      courseId: registrationCourseId.value,
      registrationToken: registrationToken.value,
      role: registrationRole.value
    })
  }

  ElMessage.success('Registration successful')
  router.push('/login')
}

const reset = () => {
  registrationForm.value?.resetFields()
}
</script>

<style scoped>
.registration-form-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f2f2f2;
  padding: 20px;
  box-sizing: border-box;
}

.registration-form {
  max-width: 500px;
  width: 100%;
  padding: 20px;
  border-radius: 10px;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
