<template>
  <div class="ram-documents">
    <div class="header">
      <div>
        <h1>Requirements Workspace</h1>
        <p class="subtitle">
          Author and manage your team's requirement documents. Start with the core set below.
        </p>
      </div>
    </div>

    <div v-if="isInstructor" class="team-selector">
      <el-select
        v-model="selectedTeamId"
        filterable
        clearable
        placeholder="Select team"
        :loading="teamsLoading"
        @change="handleInstructorTeamChange"
      >
        <el-option
          v-for="team in teams"
          :key="team.teamId"
          :label="`${team.teamName}`"
          :value="team.teamId"
        />
      </el-select>
    </div>

    <el-alert
      v-if="!hasTeam"
      type="warning"
      show-icon
      :title="isInstructor ? 'No team selected' : 'No team assigned'"
      :description="
        isInstructor
          ? 'Select a team to view and manage requirement documents.'
          : 'Ask your instructor to assign you to a team before authoring requirements.'
      "
      class="alert"
    />

    <el-skeleton v-if="documentsLoading" :rows="4" animated />

    <el-empty
      v-else-if="!visibleDocs.length"
      description="No requirement documents are available for your team yet."
    />

    <el-row v-else :gutter="16">
      <el-col v-for="doc in visibleDocs" :key="doc.type" :xs="24" :sm="12" :md="12" :lg="8">
        <el-card class="doc-card" shadow="hover">
          <div class="doc-card__header">
            <div class="doc-card__title">
              <span>{{ doc.title }}</span>
              <el-tag :type="statusTagType(docsByType[doc.type]!.status)">
                {{ docsByType[doc.type]!.status }}
              </el-tag>
            </div>
            <div class="doc-card__type">{{ doc.type }}</div>
          </div>
          <p class="doc-card__description">{{ doc.description }}</p>
          <div class="doc-card__actions">
            <el-button type="primary" :disabled="!hasTeam" @click="handleOpen(doc.type)">
              Open
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'
import { useSettingsStore } from '@/stores/settings'
import type { Instructor } from '@/apis/instructor/types'
import type { Student } from '@/apis/student/types'
import { searchDocuments } from '@/apis/ram'
import type { DocumentType, RequirementDocumentSummary } from '@/apis/ram/types'
import { searchTeams } from '@/apis/team'
import type { Team } from '@/apis/team/types'

/*
This component serves as the main dashboard for the RAM, allowing users to view and manage their requirement documents.

Both instructors and students access this page, but their experience differs based on their role:
- Instructors can select any team from a dropdown and view the requirement documents for that team. They can switch between teams to manage documents for different groups of students.
- Students are fixed to their assigned team (determined from their user info) and can only view and manage documents for that team. They do not see the team selection dropdown.

Important state variables include:
- isInstructor: Whether the current user is an instructor or student.
- studentTeamId: The team ID assigned to the student from their user info (null if not assigned). This determines which team's documents the student can see.
- routeTeamId: The team ID specified in the route query parameter, used to initialize the selected team for instructors.
- selectedTeamId: The team ID selected by the instructor from the dropdown, initialized from the route query parameter. This can be changed by the instructor to switch between teams.
- effectiveTeamId: The effective team ID to use for loading documents, which is the selected team ID for instructors and the assigned team ID for students.


1. User picks team in dropdown (v-model="selectedTeamId"), so selectedTeamId updates.
2. handleInstructorTeamChange(newTeamId) updates route.query.teamId.
3. routeTeamId recomputes.
4. Watcher on routeTeamId writes back to selectedTeamId (usually same value).

So for direct dropdown selection, step 4 is effectively a no-op.

 Why step 4 still exists:
  - It keeps dropdown state synced when route changes from other paths (back/forward, pasted URL, programmatic nav).
*/

const route = useRoute()
const router = useRouter()
const userInfoStore = useUserInfoStore()
const settingsStore = useSettingsStore()

const isInstructor = computed(() => userInfoStore.isInstructor)

// The team ID assigned to the student from their user info. This is used for students to determine which team's documents to show and is ignored for instructors (who can select any team). For students not assigned to a team, this will be null.
const studentTeamId = computed(() => (userInfoStore.userInfo as Student | null)?.teamId ?? null)

// The team ID specified in the route query parameter. This is used to initialize the selected team for instructors and is ignored for students (who are fixed to their assigned team).
const routeTeamId = computed(() => {
  const raw = route.query.teamId
  const value = Array.isArray(raw) ? raw[0] : raw
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
})

// The team ID selected by the instructor from the dropdown. This is initialized from the route query parameter and can be changed by the instructor to switch between teams. For students, this value is not used.
const selectedTeamId = ref<number | null>(routeTeamId.value)

// The effective team ID to use for loading documents, which is the selected team ID for instructors and the assigned team ID for students. This allows instructors to switch between teams while students are fixed to their own team.
const effectiveTeamId = computed(() =>
  isInstructor.value ? selectedTeamId.value : studentTeamId.value
)

const hasTeam = computed(() => Boolean(effectiveTeamId.value))

const teams = ref<Team[]>([])
const teamsLoading = ref(false)

const documentsLoading = ref(false)
const documents = ref<RequirementDocumentSummary[]>([])

// Define a catalog of core requirement document types that we want to display in the UI. Each entry includes the document type, a user-friendly title, and a brief description. This catalog serves as the basis for rendering the document cards on the page, allowing us to show all core document types even if some haven't been created yet.
const catalog = [
  {
    type: 'VISION_SCOPE' as DocumentType,
    title: 'Vision & Scope',
    description: "Define the project's goals, scope boundaries, and stakeholders."
  },
  {
    type: 'GLOSSARY' as DocumentType,
    title: 'Glossary',
    description: 'Maintain shared terminology for consistent requirements writing.'
  },
  {
    type: 'USE_CASES' as DocumentType,
    title: 'Use Cases',
    description: 'Capture user interactions and system behavior step by step.'
  },
  {
    type: 'SRS' as DocumentType,
    title: 'Software Requirements (SRS)',
    description: 'Author functional requirements, constraints, and quality attributes.'
  }
]

// Map of document type to the existing document summary for the team (if any)
const docsByType = computed<Record<DocumentType, RequirementDocumentSummary | undefined>>(() => {
  const map: Record<DocumentType, RequirementDocumentSummary | undefined> = {
    VISION_SCOPE: undefined,
    USE_CASES: undefined,
    GLOSSARY: undefined,
    SRS: undefined,
    USER_STORIES: undefined,
    BUSINESS_RULES: undefined
  }
  for (const doc of documents.value) {
    map[doc.type] = doc
  }
  return map
})

// Only show document types that have been created for the team
const visibleDocs = computed(() => catalog.filter((doc) => docsByType.value[doc.type]))

// For instructors, the team ID is determined by the dropdown selection (which is initialized from the route query parameter). For students, the team ID is determined by their assigned team from their user info. This allows instructors to switch between teams while students are fixed to their own team.
const ramQuery = computed(() =>
  isInstructor.value && effectiveTeamId.value
    ? { teamId: String(effectiveTeamId.value) }
    : undefined
)

const instructorSectionId = computed(() => {
  const settingsSectionId = settingsStore.defaultSectionId
  if (Number.isFinite(settingsSectionId) && settingsSectionId > 0) return settingsSectionId
  const instructor = userInfoStore.userInfo as Instructor | null
  const defaultSectionId = Number(instructor?.defaultSectionId)
  return Number.isFinite(defaultSectionId) && defaultSectionId > 0 ? defaultSectionId : null
})

function statusTagType(status: string) {
  switch (status) {
    case 'SUBMITTED':
      return 'warning'
    case 'RETURNED':
      return 'danger'
    default:
      return 'info'
  }
}

async function loadTeams() {
  if (!isInstructor.value) return
  const sectionId = instructorSectionId.value
  if (!sectionId) {
    teams.value = []
    return
  }
  teamsLoading.value = true
  try {
    const result = await searchTeams({ page: 0, size: 200 }, { sectionId })
    teams.value = result.data.content
  } catch {
  } finally {
    teamsLoading.value = false
  }
}

async function loadDocuments() {
  if (!effectiveTeamId.value) {
    documents.value = []
    return
  }
  documentsLoading.value = true
  try {
    const result = await searchDocuments(effectiveTeamId.value, { page: 0, size: 50 }, {})
    documents.value = result.data.content
  } finally {
    documentsLoading.value = false
  }
}

// When the instructor changes the selected team from the dropdown, update the route query parameter to reflect the new team selection. This allows the selected team to be shareable via URL and also ensures that refreshing the page will maintain the current team selection. For students, this function does nothing since they are fixed to their assigned team.
function handleInstructorTeamChange(newTeamId: number | null | undefined) {
  if (!isInstructor.value) return
  if (!newTeamId) {
    router.replace({ name: 'ram-documents' })
    return
  }
  router.replace({ name: 'ram-documents', query: { teamId: String(newTeamId) } })
}

function handleOpen(type: DocumentType) {
  const existing = docsByType.value[type]
  if (!existing) return
  if (type === 'GLOSSARY') {
    router.push({
      name: 'ram-glossary',
      params: { documentId: existing.id },
      query: ramQuery.value
    })
    return
  }

  if (type === 'USE_CASES') {
    router.push({ name: 'ram-use-cases', query: ramQuery.value })
    return
  }

  router.push({
    name: 'ram-document-editor',
    params: { documentId: existing.id },
    query: ramQuery.value
  })
}

async function ensureDocumentsLoaded() {
  if (isInstructor.value) await loadTeams()
  await loadDocuments()
}

onMounted(ensureDocumentsLoaded)

watch(
  () => routeTeamId.value,
  (value) => {
    selectedTeamId.value = value
  }
)

watch(
  () => instructorSectionId.value,
  async (value, previous) => {
    if (!isInstructor.value || !value || value === previous) return
    await loadTeams()
  }
)

// Whenever the effective team ID changes (either through the instructor changing their selection or a student being assigned to a team), reload the documents for the new team. If there is no effective team ID, clear the documents list.
watch(
  () => effectiveTeamId.value,
  async (value, previous) => {
    if (!value) {
      documents.value = []
      return
    }
    if (value === previous) return
    await loadDocuments()
  }
)
</script>

<style scoped>
.ram-documents {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header h1 {
  margin: 0;
  font-size: 28px;
}

.subtitle {
  margin-top: 4px;
  color: #6b7280;
}

.team-selector {
  max-width: 360px;
}

.alert {
  margin-bottom: 12px;
}

.doc-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.doc-card__header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.doc-card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.doc-card__type {
  font-size: 12px;
  text-transform: uppercase;
  color: #9ca3af;
}

.doc-card__description {
  color: #4b5563;
  min-height: 54px;
}

.doc-card__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
