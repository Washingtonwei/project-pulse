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

    <el-alert
      v-if="!hasTeam"
      type="warning"
      show-icon
      title="No team assigned"
      description="Ask your instructor to assign you to a team before authoring requirements."
      class="alert"
    />

    <el-skeleton v-if="loading" :rows="4" animated />

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
import { useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'
import type { Student } from '@/apis/student/types'
import { searchDocuments } from '@/apis/ram'
import type { DocumentType, RequirementDocumentSummary } from '@/apis/ram/types'

const router = useRouter()
const userInfoStore = useUserInfoStore()

const teamId = computed(() => (userInfoStore.userInfo as Student | null)?.teamId ?? null)
const hasTeam = computed(() => Boolean(teamId.value))

const loading = ref(false)

const documents = ref<RequirementDocumentSummary[]>([]) // All documents for the team, loaded from the API

// Catalog of core requirement document types with descriptions
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

async function loadDocuments() {
  if (!teamId.value) return
  loading.value = true
  try {
    const result = await searchDocuments(teamId.value, { page: 0, size: 50 }, {})
    documents.value = result.data.content
  } finally {
    loading.value = false
  }
}

function handleOpen(type: DocumentType) {
  const existing = docsByType.value[type]
  if (!existing) return
  if (type === 'GLOSSARY') {
    router.push({ name: 'ram-glossary', params: { documentId: existing.id } })
    return
  }

  if (type === 'USE_CASES') {
    router.push({ name: 'ram-use-cases' })
    return
  }

  router.push({ name: 'ram-document-editor', params: { documentId: existing.id } })
}

async function ensureDocumentsLoaded() {
  await loadDocuments()
}

onMounted(ensureDocumentsLoaded)

// Reload documents whenever the team changes (e.g. user gets assigned to a team after initially having no team)
watch(
  () => teamId.value,
  async (value, previous) => {
    if (!value || value === previous) return
    await ensureDocumentsLoaded()
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
