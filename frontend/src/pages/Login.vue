<template>
  <el-row class="login-page">
    <el-col :span="12" class="bg"></el-col>
    <el-col :span="6" :offset="3" class="form">
      <!-- Website intro: Project Pulse -->
      <h1>Project Pulse</h1>
      <p>
        A streamlined platform for senior design students to track project contributions and submit
        peer evaluations. Easily log your work, from coding to meetings, and provide meaningful
        feedback on your teammates' performance through structured weekly evaluations.
      </p>

      <el-form ref="loginForm" size="large" autocomplete="off" :model="loginData" :rules="rules">
        <el-form-item>
          <h2>Login</h2>
        </el-form-item>
        <el-form-item prop="username">
          <!-- Add icon to the input -->
          <el-input
            :prefix-icon="User"
            placeholder="Type your username here"
            v-model="loginData.username"
            @keydown.enter="login"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            name="password"
            :prefix-icon="Lock"
            show-password
            type="password"
            placeholder="Type your password here"
            v-model="loginData.password"
            @keydown.enter="login"
          ></el-input>
        </el-form-item>
        <el-form-item class="flex">
          <div class="flex">
            <el-checkbox>Remember me</el-checkbox>
            <el-link type="primary" :underline="false" @click="goToForgotPassword"
              >Forget password?</el-link
            >
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            class="button"
            type="primary"
            auto-insert-space
            @click="login"
            :loading="isLoading"
          >
            Login
          </el-button>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { User, Lock } from '@element-plus/icons-vue'
import { ref } from 'vue'
import type { LoginData } from '@/apis/login/types'

const loginData = ref<LoginData>({
  username: '',
  password: ''
})

// Add loading state
const isLoading = ref(false)

const loginForm = ref()

const rules = {
  username: [
    { required: true, message: 'Username cannot be empty.', trigger: 'blur' },
    {
      pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
      message: 'Username should be a valid email address.',
      trigger: 'blur'
    }
  ],
  password: [{ required: true, message: 'Password cannot be empty.', trigger: 'blur' }]
}

import { loginUser } from '@/apis/login'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import { useSettingsStore } from '@/stores/settings'

const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()
const settingsStore = useSettingsStore()

import type { Instructor } from '@/apis/instructor/types'
import type { Student } from '@/apis/student/types'

async function login() {
  try {
    await loginForm.value.validate() // Validate the form
  } catch (error) {
    console.error('Validation failed or an error occurred:', error)
  }

  isLoading.value = true

  try {
    const result = await loginUser(loginData.value)
    // Save token to Pinia store
    tokenStore.setToken(result.data.token)
    // Save userInfo to Pinia store
    userInfoStore.setUserInfo(result.data.userInfo)
    // Save default section and course to Pinia store if the user is an instructor
    if (userInfoStore.userInfo?.roles?.includes('instructor')) {
      settingsStore.setDefaultSectionId(
        (userInfoStore.userInfo as Instructor).defaultSectionId ?? NaN
      )
      settingsStore.setDefaultCourseId(
        (userInfoStore.userInfo as Instructor).defaultCourseId ?? NaN
      )
    }
    // Save default section to Pinia store if the user is a student
    if (userInfoStore.userInfo?.roles?.includes('student')) {
      settingsStore.setDefaultSectionId((userInfoStore.userInfo as Student).sectionId ?? NaN)
    }

    ElMessage.success('Login successfully!')

    // Redirect to the previous page or home page
    let redirect: any = route.query.redirect
    router.push({ path: redirect || '/' })
  } catch (error) {
    console.error(error)
  } finally {
    isLoading.value = false
  }
}

function goToForgotPassword() {
  router.push({ name: 'forget-password' })
}
</script>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  background-color: #fff;

  .bg {
    background:
      // url('@/assets/logo2.png') no-repeat 60% center / 240px auto,
      url('@/assets/login-bg.jpg') no-repeat center / cover;
    border-radius: 0 20px 20px 0;
  }

  .form {
    display: flex;
    flex-direction: column;
    justify-content: center;
    user-select: none;

    h1 {
      font-size: 4em;
      background: linear-gradient(to right, #409eff, #2a598a);
      -webkit-background-clip: text;
      color: transparent;
      text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
      animation:
        glow 1.5s ease-in-out infinite alternate,
        pulse 2s ease-in-out infinite;
    }

    p {
      font-size: 1.2em;
      color: #333;
    }

    @keyframes glow {
      from {
        text-shadow: 2px 2px 8px rgba(64, 158, 255, 0.8);
      }
      to {
        text-shadow: 2px 2px 20px rgba(64, 158, 255, 1);
      }
    }

    @keyframes pulse {
      0% {
        transform: scale(1);
      }
      50% {
        transform: scale(1.03);
      }
      100% {
        transform: scale(1);
      }
    }

    .title {
      margin: 0 auto;
    }

    .button {
      width: 100%;
    }

    .flex {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
  }
}
</style>
