<template>
  <el-container class="layout-container">
    <!-- Left Side Navigation Menu -->
    <el-aside :width="isCollapse ? '80px' : '240px'">
      <!-- Dynamically change the logo based on isCollapse state -->
      <div class="el-aside__logo" :style="logoStyle"></div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#232323"
        text-color="#fff"
        router
        :default-active="$route.path"
        :collapse="isCollapse"
      >
        <MenuItems :menuRoute="menuRouteStore.menuRoute"></MenuItems>
      </el-menu>
    </el-aside>
    <!-- Right Side Main Content Area -->
    <el-container>
      <!-- Header -->
      <TopNavigationBar />
      <!-- Content -->
      <el-main>
        <RouterView />
      </el-main>
      <!-- Footer -->
      <el-footer>Project Pulse {{ appVersion }}</el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import MenuItems from '@/components/MenuItems.vue'
import TopNavigationBar from '@/components/TopNavigationBar.vue'
import { useMenuRouteStore } from '@/stores/menuRoute'
import { computed, provide, ref } from 'vue'

const menuRouteStore = useMenuRouteStore()

const isCollapse = ref(false)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

// Using new URL to resolve image paths correctly in Vite
const logoStyle = computed(() => {
  const logoUrl = isCollapse.value
    ? new URL('@/assets/logo-small.png', import.meta.url).href
    : new URL('@/assets/logo.png', import.meta.url).href
  return {
    background: `url(${logoUrl}) no-repeat center`,
    backgroundSize: isCollapse.value ? '50px auto' : '200px auto',
    height: '120px' // Ensures the div has the proper height
  }
})

provide('menuCollapse', { isCollapse, toggleCollapse })

const appVersion = __APP_VERSION__
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    background-color: #232323;
    transition: all 0.3s;
    &__logo {
      transition: background-image 0.2s ease;
    }

    .el-menu {
      border-right: none;
    }
  }

  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #666;
  }
}
</style>
