import { defineStore } from 'pinia'
import { ref } from 'vue'
import { routes } from '@/router/routes'
import type { RouteRecordRaw } from 'vue-router'

export const useMenuRouteStore = defineStore(
  'menuRoute',
  () => {
    // menuRoute stores the route object defining the entire sidebar menu, it is used to dynamically generate the sidebar menu
    const menuRoute = ref<RouteRecordRaw>(routes.find((route) => route.name === 'dashboard')!) // The dashboard route is the parent route of the sidebar menu.

    return {
      menuRoute
    }
  },
  { persist: true }
)
