<template>
  <el-header>
    <div class="nav-bar-left">
      <el-icon style="margin-right: 10px" @click="toggleCollapse">
        <Expand v-if="isCollapse"></Expand>
        <Fold v-else></Fold>
      </el-icon>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item
          :to="{ name: route.name }"
          v-for="route in $route.matched"
          :key="route.path"
        >
          <div style="display: flex">
            <el-icon style="margin-right: 5px">
              <component :is="route.meta.icon" />
            </el-icon>
            <span>{{ route.meta.title }}</span>
          </div>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="nav-bar-right">
      <el-button type="info" :icon="Refresh" plain circle @click="refresh"></el-button>
      <el-button type="info" :icon="FullScreen" plain circle @click="goToFullScreen"></el-button>
      <el-dropdown placement="bottom-end" @command="handleCommand" style="margin-left: 10px">
        <span class="el-dropdown-box">
          <el-avatar :src="avatar" />
          <span style="font-size: large; font-weight: 900; margin-left: 5px">
            {{ userInfoStore.userInfo?.username }}
          </span>
          <el-icon>
            <CaretBottom />
          </el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile" :icon="User">Info</el-dropdown-item>
            <el-dropdown-item command="resetPassword" :icon="EditPen">
              Reset password
            </el-dropdown-item>
            <el-dropdown-item command="logout" :icon="SwitchButton">Log out</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import {
  Expand,
  Fold,
  ArrowRight,
  Refresh,
  FullScreen,
  CaretBottom,
  User,
  EditPen,
  SwitchButton
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'

import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import { useSettingsStore } from '@/stores/settings'
const router = useRouter()
const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()
const settingsStore = useSettingsStore()

import { inject } from 'vue'
const { isCollapse, toggleCollapse } = inject('menuCollapse')

const refresh = () => {
  router.go(0)
}

function goToFullScreen() {
  const el = document.documentElement
  const rfs = el.requestFullscreen
  if (rfs) {
    rfs.call(el)
  }
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push({ name: 'user-profile' })
      break
    case 'resetPassword':
      router.push({ name: 'user-reset-password' })
      break
    case 'logout':
      logout()
      break
    default:
      break
  }
}

import { ElMessageBox, ElMessage } from 'element-plus'
function logout() {
  ElMessageBox.confirm('Are you sure you want to log out?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning'
  })
    .then(() => {
      // Remove token, userInfo, and default section Id from Pinia store
      tokenStore.removeToken()
      userInfoStore.removeUserInfo()
      settingsStore.removeDefaultSectionId()

      // Redirect to login page
      router.push({ name: 'login' })
      ElMessage.success('Log out successfully!')
    })
    .catch(() => {
      ElMessage.info('Log out canceled!')
    })
}
</script>

<style lang="scss" scoped>
.el-header {
  background-color: #fff;
  display: flex;

  justify-content: space-between;

  .nav-bar-left {
    display: flex;
    align-items: center;
  }

  .nav-bar-right {
    display: flex;
    align-items: center;
    .el-dropdown-box {
      display: flex;
      align-items: center;

      .el-icon {
        color: #999;
        margin-left: 10px;
      }

      &:active,
      &:focus {
        outline: none;
      }
    }
  }
}
</style>
