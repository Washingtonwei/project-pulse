import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import setupNavigationGuards from './guards'

// Create router
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

setupNavigationGuards(router)

export default router
