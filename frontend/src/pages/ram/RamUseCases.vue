<template>
  <div class="ram-use-cases">
    <el-page-header content="Use Cases" @back="goBack" />

    <el-alert
      v-if="!hasTeam"
      type="warning"
      show-icon
      title="No team assigned"
      description="Ask your instructor to assign you to a team before authoring use cases."
    />

    <el-skeleton v-else-if="loading" :rows="6" animated />

    <div v-else class="ram-use-cases__layout">
      <el-card class="ram-use-cases__sidebar" shadow="never">
        <template #header>
          <div class="sidebar-header">
            <span>Use Cases</span>
            <el-button type="primary" plain size="small" @click="createNew">
              New
            </el-button>
          </div>
        </template>

        <el-input
          v-model="searchText"
          placeholder="Search by title"
          clearable
          @input="filterList"
          class="search"
        />

        <el-menu
          class="usecase-menu"
          :default-active="String(selectedUseCaseId || '')"
          @select="handleSelect"
        >
          <el-menu-item v-for="item in filteredUseCases" :key="item.id" :index="String(item.id)">
            <div class="menu-item">
              <div class="menu-title-row">
                <span class="menu-key">{{ item.artifactKey }}</span>
                <span class="menu-title">{{ item.title }}</span>
                <el-tag v-if="isEditing(item.id)" size="small" type="success">Editing</el-tag>
              </div>
            </div>
          </el-menu-item>
        </el-menu>
      </el-card>

      <el-card class="ram-use-cases__content" shadow="never">
        <template #header>
          <div class="content-header">
            <div>
              <h2>{{ isNew ? 'New Use Case' : currentUseCase.title || 'Use Case' }}</h2>
              <div class="meta" v-if="currentUseCase.artifactKey">
                {{ currentUseCase.artifactKey }}
              </div>
              <div v-if="editingNotice" class="editing-note">{{ editingNotice }}</div>
            </div>
            <el-button type="success" :loading="saving" @click="saveUseCase">
              Save
            </el-button>
          </div>
        </template>

        <el-empty
          v-if="!isNew && !selectedUseCaseId"
          description="Select a use case on the left or create a new one."
        />

        <el-form v-else label-width="140px" class="usecase-form">
          <el-form-item label="Title">
            <el-input v-model="currentUseCase.title" placeholder="Use case title" />
          </el-form-item>
          <el-form-item label="Description">
            <el-input v-model="currentUseCase.description" type="textarea" :autosize="{ minRows: 3 }" />
          </el-form-item>
          <el-form-item label="Trigger">
            <el-input v-model="currentUseCase.trigger" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
          <el-form-item label="Primary Actor">
            <el-select v-model="currentUseCase.primaryActorId" filterable placeholder="Select primary actor">
              <el-option
                v-for="actor in stakeholderOptions"
                :key="actor.id"
                :label="formatActorLabel(actor)"
                :value="actor.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Secondary Actors">
            <el-select
              v-model="currentUseCase.secondaryActorIds"
              multiple
              filterable
              placeholder="Select secondary actors"
            >
              <el-option
                v-for="actor in stakeholderOptions"
                :key="actor.id"
                :label="formatActorLabel(actor)"
                :value="actor.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Priority">
            <el-select v-model="currentUseCase.priority" placeholder="Priority">
              <el-option v-for="priority in priorities" :key="priority" :label="priority" :value="priority" />
            </el-select>
          </el-form-item>
          <el-form-item label="Notes">
            <el-input v-model="currentUseCase.notes" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
        </el-form>

        <template v-if="isNew || selectedUseCaseId">
          <el-divider>Preconditions</el-divider>
          <div class="conditions">
            <div v-for="(condition, index) in currentUseCase.preconditions" :key="index" class="condition-item">
              <el-input v-model="condition.condition" placeholder="Condition" />
              <el-button type="danger" plain @click="removePrecondition(index)">Remove</el-button>
            </div>
            <el-button type="primary" plain @click="addPrecondition">Add precondition</el-button>
          </div>

          <el-divider>Postconditions</el-divider>
          <div class="conditions">
            <div v-for="(condition, index) in currentUseCase.postconditions" :key="index" class="condition-item">
              <el-input v-model="condition.condition" placeholder="Condition" />
              <el-button type="danger" plain @click="removePostcondition(index)">Remove</el-button>
            </div>
            <el-button type="primary" plain @click="addPostcondition">Add postcondition</el-button>
          </div>

          <el-divider>Main Steps</el-divider>
          <div class="steps">
            <div class="step-between step-between--top">
              <div class="step-insert" @click="addStepAt(0)">
                <el-button circle plain size="small" icon="Plus" />
                <span>Insert step</span>
              </div>
            </div>

            <div
              v-for="(step, stepIndex) in currentUseCase.mainSteps"
              :key="stepIndex"
              class="step-wrapper"
            >
              <div
                class="step-card"
                :class="{ 'step-card--active': stepIndex === activeStepIndex }"
                @click="setActiveStep(stepIndex)"
              >
                <div class="step-header">
                  <div class="step-header-left">
                    <strong>Step {{ stepIndex + 1 }}</strong>
                    <el-button
                      v-if="step.extensions.length"
                      text
                      size="small"
                      class="extensions-toggle"
                      @click="toggleExtensions(stepIndex)"
                    >
                      <el-icon>
                        <component :is="isExtensionsOpen(stepIndex) ? 'CaretBottom' : 'CaretRight'" />
                      </el-icon>
                      Extensions ({{ step.extensions.length }})
                    </el-button>
                  </div>
                  <div class="step-header-actions">
                    <el-tooltip content="Add extension" placement="top">
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        circle
                        icon="Connection"
                        @click="addExtension(stepIndex)"
                      />
                    </el-tooltip>
                    <el-tooltip content="Remove step" placement="top">
                      <el-button
                        type="danger"
                        plain
                        size="small"
                        circle
                        icon="Delete"
                        @click="removeStep(stepIndex)"
                      />
                    </el-tooltip>
                  </div>
                </div>
                <el-input v-model="step.actor" class="actor-hidden" />
                <el-input
                  :ref="(el: any) => setActionRef(el, stepIndex)"
                  v-model="step.actionText"
                  placeholder="Action"
                  type="textarea"
                  :autosize="{ minRows: 2 }"
                />

                <div v-show="isExtensionsOpen(stepIndex)" class="extensions">
                  <h4>Extensions</h4>
                  <div
                    v-for="(ext, extIndex) in step.extensions"
                    :key="extIndex"
                    class="extension-card"
                  >
                    <div class="extension-header">
                      <span>Extension {{ extIndex + 1 }}</span>
                      <el-button type="danger" plain size="small" @click="removeExtension(stepIndex, extIndex)">
                        Remove
                      </el-button>
                    </div>
                    <el-input v-model="ext.conditionText" placeholder="Condition" />
                    <div class="extension-meta">
                      <el-select v-model="ext.kind" placeholder="Kind">
                        <el-option label="Alternate" value="ALTERNATE" />
                        <el-option label="Exception" value="EXCEPTION" />
                      </el-select>
                      <el-select v-model="ext.exit" placeholder="Exit">
                        <el-option label="Resume" value="RESUME" />
                        <el-option label="End Success" value="END_SUCCESS" />
                        <el-option label="End Failure" value="END_FAILURE" />
                      </el-select>
                    </div>

                    <div class="extension-steps">
                      <div
                        v-for="(extStep, extStepIndex) in ext.steps"
                        :key="extStepIndex"
                        class="extension-step"
                      >
                        <el-input v-model="extStep.actor" class="actor-hidden" />
                        <el-input v-model="extStep.actionText" placeholder="Action" />
                        <el-button
                          type="danger"
                          plain
                          size="small"
                          @click="removeExtensionStep(stepIndex, extIndex, extStepIndex)"
                        >
                          Remove
                        </el-button>
                      </div>
                      <el-button type="primary" plain size="small" @click="addExtensionStep(stepIndex, extIndex)">
                        Add extension step
                      </el-button>
                    </div>
                  </div>
                  <el-button type="primary" plain size="small" @click="addExtension(stepIndex)">
                    Add extension
                  </el-button>
                </div>
              </div>
              <div class="step-between">
                <div class="step-insert" @click="addStepAt(stepIndex + 1)">
                  <el-button circle plain size="small" icon="Plus" />
                  <span>Insert step</span>
                </div>
              </div>
            </div>

            <div class="step-between step-between--bottom">
              <div class="step-insert" @click="addStepAt(currentUseCase.mainSteps.length)">
                <el-button circle plain size="small" icon="Plus" />
                <span>Insert step</span>
              </div>
            </div>

          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import type { Student } from '@/apis/student/types'
import type {
  RequirementArtifact,
  UseCaseDto
} from '@/apis/ram/types'
import {
  searchRequirementArtifacts,
  getUseCaseById,
  createUseCase,
  updateUseCase
} from '@/apis/ram'

const router = useRouter()
const userInfoStore = useUserInfoStore()

const teamId = computed(() => (userInfoStore.userInfo as Student | null)?.teamId ?? null)
const hasTeam = computed(() => Boolean(teamId.value))

const loading = ref(false)
const saving = ref(false)
const searchText = ref('')

const useCases = ref<RequirementArtifact[]>([])
const filteredUseCases = ref<RequirementArtifact[]>([])
const selectedUseCaseId = ref<number | null>(null)
const isNew = ref(false)

const stakeholderOptions = ref<RequirementArtifact[]>([])

const currentUseCase = ref<UseCaseDto>(createEmptyUseCase())

const priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

const editingMap = ref<Record<number, string>>({})
const activeStepIndex = ref<number | null>(null)
const actionRefs = ref<Record<number, any>>({})

const editingNotice = computed(() => {
  if (!selectedUseCaseId.value) return ''
  const startedAt = editingMap.value[selectedUseCaseId.value]
  if (!startedAt) return ''
  return `Editing in this session since ${new Date(startedAt).toLocaleTimeString()}`
})

function isEditing(id?: number) {
  if (!id) return false
  return Boolean(editingMap.value[id])
}

function createEmptyUseCase(): UseCaseDto {
  return {
    title: '',
    description: '',
    teamId: teamId.value || 0,
    primaryActorId: 0,
    secondaryActorIds: [],
    trigger: '',
    preconditions: [],
    postconditions: [],
    mainSteps: [],
    priority: 'MEDIUM',
    notes: ''
  }
}

function goBack() {
  router.push({ name: 'ram-documents' })
}

async function loadUseCases() {
  if (!teamId.value) return
  const result = await searchRequirementArtifacts(teamId.value, { page: 0, size: 200 }, { type: 'USE_CASE' })
  useCases.value = result.data.content
  filterList()
}

async function loadStakeholders() {
  if (!teamId.value) return
  const result = await searchRequirementArtifacts(teamId.value, { page: 0, size: 500 }, {})
  stakeholderOptions.value = result.data.content.filter((artifact) =>
    ['STAKEHOLDER', 'EXTERNAL_INTERFACE_REQUIREMENT'].includes(artifact.type)
  )
}

function filterList() {
  const query = searchText.value.trim().toLowerCase()
  if (!query) {
    filteredUseCases.value = [...useCases.value]
    return
  }
  filteredUseCases.value = useCases.value.filter((item) => item.title.toLowerCase().includes(query))
}

async function handleSelect(id: string) {
  if (!teamId.value) return
  selectedUseCaseId.value = Number(id)
  isNew.value = false
  const result = await getUseCaseById(teamId.value, selectedUseCaseId.value)
  currentUseCase.value = normalizeUseCase(result.data)
  markEditing(selectedUseCaseId.value)
  activeStepIndex.value = null
}

function createNew() {
  selectedUseCaseId.value = null
  isNew.value = true
  currentUseCase.value = createEmptyUseCase()
  activeStepIndex.value = null
}

function normalizeUseCase(useCase: UseCaseDto): UseCaseDto {
  return {
    ...useCase,
    secondaryActorIds: useCase.secondaryActorIds || [],
    preconditions: useCase.preconditions || [],
    postconditions: useCase.postconditions || [],
    mainSteps: useCase.mainSteps || []
  }
}

function formatActorLabel(actor: RequirementArtifact) {
  if (!actor) return ''
  const key = actor.artifactKey ? `${actor.artifactKey} — ` : ''
  return `${key}${actor.title}`
}

function markEditing(id: number) {
  editingMap.value = {
    ...editingMap.value,
    [id]: new Date().toISOString()
  }
}

function addPrecondition() {
  currentUseCase.value.preconditions = currentUseCase.value.preconditions || []
  currentUseCase.value.preconditions.push({ condition: '', type: 'PRECONDITION' })
}

function removePrecondition(index: number) {
  currentUseCase.value.preconditions?.splice(index, 1)
}

function addPostcondition() {
  currentUseCase.value.postconditions = currentUseCase.value.postconditions || []
  currentUseCase.value.postconditions.push({ condition: '', type: 'POSTCONDITION' })
}

function removePostcondition(index: number) {
  currentUseCase.value.postconditions?.splice(index, 1)
}

function addStep() {
  currentUseCase.value.mainSteps.push({ actor: '', actionText: '', extensions: [] })
  focusStep(currentUseCase.value.mainSteps.length - 1)
}

function addStepAt(index: number) {
  currentUseCase.value.mainSteps.splice(index, 0, { actor: '', actionText: '', extensions: [] })
  focusStep(index)
}

function removeStep(index: number) {
  currentUseCase.value.mainSteps.splice(index, 1)
  if (activeStepIndex.value === index) {
    activeStepIndex.value = null
  }
}

function setActiveStep(index: number) {
  activeStepIndex.value = index
}

function setActionRef(el: any, index: number) {
  if (!el) return
  actionRefs.value[index] = el
}

async function focusStep(index: number) {
  activeStepIndex.value = index
  await nextTick()
  const input = actionRefs.value[index]
  if (input && typeof input.focus === 'function') {
    input.focus()
  }
}

function addExtension(stepIndex: number) {
  const step = currentUseCase.value.mainSteps[stepIndex]
  step.extensions.push({ conditionText: '', kind: 'ALTERNATE', exit: 'RESUME', steps: [] })
  openExtensions(stepIndex)
}

function removeExtension(stepIndex: number, extIndex: number) {
  const step = currentUseCase.value.mainSteps[stepIndex]
  step.extensions.splice(extIndex, 1)
  if (!step.extensions.length) closeExtensions(stepIndex)
}

function addExtensionStep(stepIndex: number, extIndex: number) {
  const extension = currentUseCase.value.mainSteps[stepIndex].extensions[extIndex]
  extension.steps.push({ actor: '', actionText: '' })
}

function removeExtensionStep(stepIndex: number, extIndex: number, extStepIndex: number) {
  const extension = currentUseCase.value.mainSteps[stepIndex].extensions[extIndex]
  extension.steps.splice(extStepIndex, 1)
}

const openExtensionsMap = ref<Record<number, boolean>>({})

function isExtensionsOpen(stepIndex: number) {
  return Boolean(openExtensionsMap.value[stepIndex])
}

function toggleExtensions(stepIndex: number) {
  openExtensionsMap.value = {
    ...openExtensionsMap.value,
    [stepIndex]: !openExtensionsMap.value[stepIndex]
  }
}

function openExtensions(stepIndex: number) {
  openExtensionsMap.value = { ...openExtensionsMap.value, [stepIndex]: true }
}

function closeExtensions(stepIndex: number) {
  openExtensionsMap.value = { ...openExtensionsMap.value, [stepIndex]: false }
}

function validateUseCase(): boolean {
  if (!currentUseCase.value.title.trim()) {
    ElMessage.warning('Title is required')
    return false
  }
  if (!currentUseCase.value.description.trim()) {
    ElMessage.warning('Description is required')
    return false
  }
  if (!currentUseCase.value.trigger.trim()) {
    ElMessage.warning('Trigger is required')
    return false
  }
  if (!currentUseCase.value.primaryActorId) {
    ElMessage.warning('Primary actor is required')
    return false
  }
  return true
}

async function saveUseCase() {
  if (!teamId.value) return
  if (!validateUseCase()) return

  saving.value = true
  try {
    const sanitizeActor = (value?: string) => {
      const trimmed = value?.trim()
      return trimmed ? trimmed : 'N/A'
    }

    const payload = {
      ...currentUseCase.value,
      teamId: teamId.value,
      mainSteps: currentUseCase.value.mainSteps.map((step) => ({
        ...step,
        actor: sanitizeActor(step.actor),
        extensions: step.extensions.map((extension) => ({
          ...extension,
          steps: extension.steps.map((extStep) => ({
            ...extStep,
            actor: sanitizeActor(extStep.actor)
          }))
        }))
      })),
      preconditions: (currentUseCase.value.preconditions || []).map((condition) => ({
        ...condition,
        type: 'PRECONDITION'
      })),
      postconditions: (currentUseCase.value.postconditions || []).map((condition) => ({
        ...condition,
        type: 'POSTCONDITION'
      }))
    }

    const result = isNew.value || !currentUseCase.value.id
      ? await createUseCase(teamId.value, payload)
      : await updateUseCase(teamId.value, currentUseCase.value.id, payload)

    currentUseCase.value = normalizeUseCase(result.data)
    selectedUseCaseId.value = result.data.id || null
    isNew.value = false
    await loadUseCases()
    if (selectedUseCaseId.value) markEditing(selectedUseCaseId.value)
    ElMessage.success('Use case saved')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!teamId.value) return
  loading.value = true
  try {
    await Promise.all([loadUseCases(), loadStakeholders()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.ram-use-cases {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ram-use-cases :deep(.el-input__inner),
.ram-use-cases :deep(.el-textarea__inner) {
  font-family: inherit;
}

.ram-use-cases__layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  align-items: stretch;
}

.ram-use-cases__sidebar {
  min-height: 400px;
  height: calc(100vh - 170px);
  overflow: auto;
}

.ram-use-cases__content {
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

.usecase-menu {
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
  max-width: 150px;
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

.editing-note {
  font-size: 12px;
  color: #10b981;
}

.meta {
  font-size: 12px;
  color: #6b7280;
}

.usecase-form {
  margin-bottom: 12px;
}

.usecase-form :deep(.el-input__inner),
.usecase-form :deep(.el-textarea__inner) {
  font-family: inherit;
}

.conditions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.condition-item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: center;
}

.steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.step-insert {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 12px;
  justify-content: center;
  width: 100%;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.step-between {
  min-height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-between .step-insert {
  opacity: 0;
}

.step-between:hover .step-insert {
  opacity: 1;
}

.step-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-card--active {
  border-color: #409eff;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.2);
}

.steps :deep(.el-input__inner),
.steps :deep(.el-textarea__inner) {
  font-family: inherit;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-header-left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.step-header-actions {
  display: inline-flex;
  gap: 8px;
}

.actor-hidden {
  display: none;
}

.extensions-toggle {
  color: #409eff;
}

.extensions {
  border-top: 1px dashed #ebeef5;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.extension-card {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.extension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.extension-meta {
  display: flex;
  gap: 8px;
}

.extension-steps {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.extension-step {
  display: grid;
  grid-template-columns: 1fr 2fr auto;
  gap: 8px;
  align-items: center;
}

@media (max-width: 960px) {
  .ram-use-cases__layout {
    grid-template-columns: 1fr;
  }

  .ram-use-cases__sidebar,
  .ram-use-cases__content {
    height: auto;
  }

  .condition-item {
    grid-template-columns: 1fr;
  }

  .extension-step {
    grid-template-columns: 1fr;
  }
}
</style>
