# Frontend CLAUDE.md

## Key References

- **Element Plus**: https://element-plus.org/ — always consult for component APIs, props, events, and slots
- **Vue 3 Composition API**: this project uses `<script setup>` and Composition API exclusively. Never generate Options API code (`data()`, `methods`, `computed`, `watch` as object properties, etc.)

## Adding a New Feature

**Reference implementation:** Use the `activity` feature as the canonical example. It covers the full stack: API module (`src/apis/activity/`), pages (`src/pages/activities/`), and route definitions in `src/router/routes.ts`. Read these end-to-end before building a new feature.

## Adding a New API Module

Each API domain lives in `src/apis/<feature>/` with two files:

### `types.ts`
- Define the data interface (e.g., `Activity`, `Course`)
- Define response types wrapping the backend `Result` envelope: `{ flag, code, message, data }`
- Define search criteria and pagination interfaces if needed
- Use `PaginationParams` from existing types for paginated endpoints

### `index.ts`
- Define an `API` enum with endpoint paths (relative to `/api/v1`)
- Export named functions for each API call using the shared `request` instance from `@/utils/request`
- Type the return as `request.get<any, YourResponseType>(...)` — the response interceptor unwraps Axios responses

Example pattern:
```typescript
import request from '@/utils/request'
import type { FooResponse, CreateFooResponse } from './types'

enum API {
  FOO_ENDPOINT = '/foos'
}

export const searchFoos = (params: PaginationParams, criteria: FooCriteria) =>
  request.post<any, SearchFooResponse>(API.FOO_ENDPOINT + '/search', criteria, { params })

export const createFoo = (foo: Foo) =>
  request.post<any, CreateFooResponse>(API.FOO_ENDPOINT, foo)
```

## Conventions

### Pages and Components
- Pages (routable views) go in `src/pages/<feature>/`
- Use Element Plus components (`el-table`, `el-form`, `el-dialog`, etc.)
- Rich text editing uses TipTap (`@tiptap/vue-3`)
- **Naming convention for new pages** — use entity-first, PascalCase, multi-word names:
  - List pages: `<Entities>.vue` (e.g., `Students.vue`)
  - Form pages: `<Entity><Action>Form.vue` (e.g., `ActivityAddForm.vue`, `ActivityEditForm.vue`)
  - Detail/dashboard pages: `<Entity><Purpose>.vue` (e.g., `StudentPerformanceDashboard.vue`)
  - Entity-first naming groups related files together alphabetically
  - NOTE: some existing pages use verb-first naming (e.g., `AddActivityForm.vue`, `EditStudentForm.vue`) — these should be refactored to entity-first in the future

### Router
- Add routes to `src/router/routes.ts`
- Route `meta` fields control access:
  - `requiresAuth: true` — must be logged in
  - `visitorOnly: true` — only for logged-out users (login, register)
  - `requiresPermissions: ['student']` — role-based access (matches any listed role)
  - `isMenuItem: true` — appears in sidebar navigation

### State Management
- Pinia stores in `src/stores/`
- `token.ts` — JWT string, persisted to localStorage
- `userInfo.ts` — decoded user details and role list
- Access stores with `useXxxStore()` composables

### HTTP Client (`src/utils/request.ts`)
- Shared Axios instance with Bearer token auto-injection
- Response interceptor unwraps `response.data` (you get the `Result` object directly, not `AxiosResponse`)
- 401 responses auto-redirect to login and clear token/user stores
- Errors show `ElMessage.error()` toasts (with debouncing)

### Styling
- Global styles in `src/assets/main.scss`
- Component-scoped styles with `<style scoped>`
- Element Plus theming via CSS variables
