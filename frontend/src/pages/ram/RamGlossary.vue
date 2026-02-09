<template>
  <div class="ram-glossary">
    <el-page-header content="Glossary" @back="goBack" />

    <el-alert
      v-if="!hasTeam"
      type="warning"
      show-icon
      title="No team assigned"
      description="Ask your instructor to assign you to a team before authoring glossary terms."
    />

    <el-skeleton v-else-if="loading" :rows="6" animated />

    <div v-else class="ram-glossary__layout">
      <el-card class="ram-glossary__sidebar" shadow="never">
        <template #header>
          <div class="sidebar-header">
            <span>Glossary</span>
            <el-button type="primary" plain size="small" @click="openCreate">
              New
            </el-button>
          </div>
        </template>

        <el-input
          v-model="searchText"
          placeholder="Search term"
          clearable
          @input="filterList"
          class="search"
        />

        <el-menu
          class="glossary-menu"
          :default-active="String(selectedTermId || '')"
          @select="handleSelect"
        >
          <el-menu-item v-for="item in filteredTerms" :key="item.id" :index="String(item.id)">
            <div class="menu-item">
              <div class="menu-title-row">
                <span class="menu-key">{{ item.artifactKey }}</span>
                <span class="menu-title">{{ item.title }}</span>
              </div>
            </div>
          </el-menu-item>
        </el-menu>
        <el-empty v-if="!filteredTerms.length" description="No glossary terms yet." />
      </el-card>

      <el-card class="ram-glossary__content" shadow="never">
        <template #header>
          <div class="content-header">
            <div>
              <h2>{{ currentTerm.title || 'Glossary Term' }}</h2>
              <div class="meta" v-if="currentTerm.artifactKey">
                {{ currentTerm.artifactKey }}
              </div>
            </div>
            <div class="content-actions">
              <el-button
                type="primary"
                :disabled="!selectedTermId"
                :loading="saving"
                @click="saveDefinition"
              >
                Save Definition
              </el-button>
            </div>
          </div>
        </template>

        <el-empty
          v-if="!selectedTermId"
          description="Select a term on the left or create a new one."
        />

        <div v-else class="term-body">
          <el-form label-width="140px">
            <el-form-item label="Term">
              <el-input v-model="currentTerm.title" disabled />
              <el-button type="primary" text @click="openRename">Rename</el-button>
            </el-form-item>
            <el-form-item label="Definition">
              <el-input
                v-model="currentTerm.content"
                type="textarea"
                :autosize="{ minRows: 6 }"
                placeholder="Define the term"
              />
            </el-form-item>
            <el-form-item label="Notes">
              <el-input
                v-model="currentTerm.notes"
                type="textarea"
                :autosize="{ minRows: 2 }"
                placeholder="Optional notes"
              />
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="createDialogVisible" title="New Glossary Term" width="480px">
      <el-form label-width="120px">
        <el-form-item label="Term">
          <el-input v-model="newTerm.title" placeholder="Term name" />
        </el-form-item>
        <el-form-item label="Definition">
          <el-input v-model="newTerm.content" type="textarea" :autosize="{ minRows: 4 }" />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="newTerm.notes" type="textarea" :autosize="{ minRows: 2 }" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="createTerm">Create</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="renameDialogVisible" title="Rename Term" width="420px">
      <el-form label-width="120px">
        <el-form-item label="New term">
          <el-input v-model="renameValue" placeholder="New term name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="renameTerm">Rename</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import type { Student } from '@/apis/student/types'
import type { RequirementArtifact } from '@/apis/ram/types'
import {
  searchRequirementArtifacts,
  createGlossaryTerm,
  getGlossaryTermById,
  updateGlossaryTermDefinition,
  renameGlossaryTerm
} from '@/apis/ram'

const router = useRouter()
const userInfoStore = useUserInfoStore()

const teamId = computed(() => (userInfoStore.userInfo as Student | null)?.teamId ?? null)
const hasTeam = computed(() => Boolean(teamId.value))

const loading = ref(false)
const saving = ref(false)
const searchText = ref('')

const glossaryTerms = ref<RequirementArtifact[]>([])
const filteredTerms = ref<RequirementArtifact[]>([])
const selectedTermId = ref<number | null>(null)

const currentTerm = ref<RequirementArtifact>({
  type: 'GLOSSARY_TERM',
  title: '',
  content: ''
})

const createDialogVisible = ref(false)
const renameDialogVisible = ref(false)
const newTerm = ref<RequirementArtifact>({
  type: 'GLOSSARY_TERM',
  title: '',
  content: ''
})
const renameValue = ref('')

function goBack() {
  router.push({ name: 'ram-documents' })
}

async function loadGlossary() {
  if (!teamId.value) return
  try {
    const result = await searchRequirementArtifacts(
      teamId.value,
      { page: 0, size: 200 },
      { type: 'GLOSSARY_TERM' }
    )
    glossaryTerms.value = result.data.content
    filterList()
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to load glossary'
    ElMessage.error(message)
  }
}

function filterList() {
  const query = searchText.value.trim().toLowerCase()
  if (!query) {
    filteredTerms.value = [...glossaryTerms.value]
    return
  }
  filteredTerms.value = glossaryTerms.value.filter((item) => item.title.toLowerCase().includes(query))
}

async function handleSelect(id: string) {
  if (!teamId.value) return
  selectedTermId.value = Number(id)
  try {
    const result = await getGlossaryTermById(teamId.value, selectedTermId.value)
    currentTerm.value = result.data
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to load glossary term'
    ElMessage.error(message)
  }
}

function openCreate() {
  newTerm.value = { type: 'GLOSSARY_TERM', title: '', content: '' }
  createDialogVisible.value = true
}

async function createTerm() {
  if (!teamId.value) return
  if (!newTerm.value.title.trim() || !newTerm.value.content.trim()) {
    ElMessage.warning('Term and definition are required')
    return
  }
  saving.value = true
  try {
    const payload = { ...newTerm.value, type: 'GLOSSARY_TERM' }
    const result = await createGlossaryTerm(teamId.value, payload)
    glossaryTerms.value.unshift(result.data)
    filterList()
    selectedTermId.value = result.data.id || null
    currentTerm.value = result.data
    createDialogVisible.value = false
    ElMessage.success('Glossary term created')
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to create glossary term'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

function openRename() {
  renameValue.value = currentTerm.value.title || ''
  renameDialogVisible.value = true
}

async function renameTerm() {
  if (!teamId.value || !selectedTermId.value) return
  if (!renameValue.value.trim()) {
    ElMessage.warning('Term is required')
    return
  }
  saving.value = true
  try {
    const payload = { ...currentTerm.value, title: renameValue.value }
    const result = await renameGlossaryTerm(teamId.value, selectedTermId.value, payload)
    currentTerm.value = result.data
    const idx = glossaryTerms.value.findIndex((term) => term.id === selectedTermId.value)
    if (idx >= 0) glossaryTerms.value[idx] = result.data
    filterList()
    renameDialogVisible.value = false
    ElMessage.success('Glossary term renamed')
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to rename glossary term'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

async function saveDefinition() {
  if (!teamId.value || !selectedTermId.value) return
  saving.value = true
  try {
    const payload = { ...currentTerm.value, type: 'GLOSSARY_TERM' }
    const result = await updateGlossaryTermDefinition(teamId.value, selectedTermId.value, payload)
    currentTerm.value = result.data
    const idx = glossaryTerms.value.findIndex((term) => term.id === selectedTermId.value)
    if (idx >= 0) glossaryTerms.value[idx] = result.data
    filterList()
    ElMessage.success('Definition saved')
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to save definition'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

async function ensureGlossaryLoaded() {
  if (!teamId.value) return
  loading.value = true
  try {
    await loadGlossary()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await ensureGlossaryLoaded()
})

watch(
  () => teamId.value,
  async (value, previous) => {
    if (!value || value === previous) return
    await ensureGlossaryLoaded()
  }
)
</script>

<style scoped>
.ram-glossary {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ram-glossary :deep(.el-input__inner),
.ram-glossary :deep(.el-textarea__inner) {
  font-family: inherit;
}

.ram-glossary__layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  align-items: stretch;
}

.ram-glossary__sidebar {
  min-height: 400px;
  height: calc(100vh - 170px);
  overflow: auto;
}

.ram-glossary__content {
  height: calc(100vh - 170px);
  overflow: auto;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search {
  margin-bottom: 12px;
}

.glossary-menu {
  border-right: none;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.menu-title-row {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.menu-title {
  font-weight: 600;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-key {
  font-size: 12px;
  color: #6b7280;
  margin-right: 6px;
  white-space: nowrap;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.content-actions {
  display: flex;
  gap: 8px;
}

.meta {
  font-size: 12px;
  color: #6b7280;
}

.term-body {
  margin-top: 12px;
}

@media (max-width: 960px) {
  .ram-glossary__layout {
    grid-template-columns: 1fr;
  }

  .ram-glossary__sidebar,
  .ram-glossary__content {
    height: auto;
  }
}
</style>
