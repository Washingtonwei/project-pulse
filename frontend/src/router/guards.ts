import type { RouteLocationNormalized, Router } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import { jwtDecode } from 'jwt-decode'
/**
 * If a page is visitor-only, it means that the page is NOT accessible to logged-in users.
 * If a page requires authentication, it means that the page is only accessible to logged-in users.
 * If a page requires permissions, it means that the page is only accessible to users with the required permissions.
 *
 * Some pages may not require authentication, but they are not visitor-only. E.g., artifact page.
 *
 * requireAuth: false, visitorOnly: true (OK) login page, register page, etc.
 * requireAuth: false, visitorOnly: false (OK) artifacts page, etc.
 * requireAuth: true, visitorOnly: false (OK) users page, etc.
 * requireAuth: true, visitorOnly: true (Not possible)
 *
 * Case 1: If the user is not logged in and tries to visit a visitor-only page. E.g., login page, register page, etc.
 * Case 2: If the user is not logged in and tries to visit a page that requires authentication.
 * Case 3: If the user is logged in and tries to visit a visitor-only page.
 * Case 4: If the user is logged in and tries to visit a page that requires permissions.
 */
function setupNavigationGuards(router: Router) {
  // Navigation guard checks both authentication and authorization (permissions) before each route navigation
  router.beforeEach((to, from) => {
    if (!isLoggedIn()) {
      // If user is not logged in
      if (to.meta.requiresAuth) {
        return { name: 'login', query: { redirect: to.path } } // Redirect to login page if user is not logged in
      }
    } else {
      // If user is logged in
      if (isVisitorOnly(to)) {
        return from
      } else {
        // Check if the user has the required permissions
        if (!checkPermissions(to)) {
          return { name: 'forbidden' } // Redirect to 403 page if user does not have the required permissions
        }
      }
    }
  })
}

function isTokenValid(token: string | null): boolean {
  if (!token) return false // Token is not present

  try {
    const decodedToken = jwtDecode(token) // Decode the JWT token

    // Check if the token has expired
    if (decodedToken.exp! * 1000 < Date.now()) {
      return false // Token has expired
    }

    return true // Token is valid and not expired
  } catch (error) {
    return false // Token is invalid or not properly formatted
  }
}

function isLoggedIn(): boolean {
  // Check if the user is logged in
  const tokenStore = useTokenStore()
  const token = tokenStore.token

  // Validate token presence and its expiration
  return isTokenValid(token)
}

// Check if the authenticated user has the required authorization or permissions
function checkPermissions(to: RouteLocationNormalized) {
  // Get the user's permissions from the store
  const userInfoStore = useUserInfoStore()
  const userPermissions = userInfoStore.roleList
  // Check if the route requires permissions
  if (to.meta.requiresPermissions) {
    // Get the required permissions from the route
    const requiredPermissions: string[] = to.meta.requiresPermissions as string[]
    // A route can allow multiple roles; user needs any one of them.
    const hasPermissions = requiredPermissions.some((permission) =>
      userPermissions.includes(permission.toLowerCase())
    )
    return hasPermissions
  } else {
    return true
  }
}

function isVisitorOnly(route: RouteLocationNormalized): boolean {
  // Check if the route is visitor only
  return Boolean(route.meta.visitorOnly)
}

export default setupNavigationGuards
