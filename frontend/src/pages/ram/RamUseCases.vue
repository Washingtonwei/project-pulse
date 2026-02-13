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
            <el-button type="primary" plain size="small" @click="createNew"> New </el-button>
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
        <el-empty v-if="!filteredUseCases.length" description="No use cases yet." />
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
            <el-button type="success" :loading="saving" :disabled="!canSave" @click="saveUseCase">
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
            <el-input
              v-model="currentUseCase.description"
              type="textarea"
              :autosize="{ minRows: 3 }"
            />
          </el-form-item>
          <el-form-item label="Trigger">
            <el-input v-model="currentUseCase.trigger" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
          <el-form-item label="Primary Actor">
            <el-select
              v-model="currentUseCase.primaryActorId"
              filterable
              placeholder="Select primary actor"
            >
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
              <el-option
                v-for="priority in PRIORITIES"
                :key="priority"
                :label="priority"
                :value="priority"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Notes">
            <el-input v-model="currentUseCase.notes" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
        </el-form>

        <template v-if="isNew || selectedUseCaseId">
          <el-divider>Preconditions</el-divider>
          <div class="conditions">
            <div
              v-for="(condition, index) in currentUseCase.preconditions"
              :key="index"
              class="condition-item"
            >
              <el-input v-model="condition.condition" placeholder="Condition" />
              <el-button type="danger" plain @click="removePrecondition(index)">Remove</el-button>
            </div>
            <el-button type="primary" plain @click="addPrecondition">Add precondition</el-button>
          </div>

          <el-divider>Postconditions</el-divider>
          <div class="conditions">
            <div
              v-for="(condition, index) in currentUseCase.postconditions"
              :key="index"
              class="condition-item"
            >
              <el-input v-model="condition.condition" placeholder="Condition" />
              <el-button type="danger" plain @click="removePostcondition(index)">Remove</el-button>
            </div>
            <el-button type="primary" plain @click="addPostcondition">Add postcondition</el-button>
          </div>

          <el-divider>Main Steps</el-divider>
          <div class="steps">
            <div class="step-between step-between--top">
              <div class="step-insert">
                <el-button circle plain size="small" icon="Plus" @click="addStepAt(0)" />
                <span @click="addStepAt(0)">Insert step</span>
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
                        <component
                          :is="isExtensionsOpen(stepIndex) ? 'CaretBottom' : 'CaretRight'"
                        />
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
                <div class="step-body">
                  <el-input v-model="step.actor" class="actor-hidden" />
                  <el-input
                    :ref="(el: any) => setActionRef(el, stepIndex)"
                    v-model="step.actionText"
                    placeholder="Action"
                    type="textarea"
                    :autosize="{ minRows: 2 }"
                  />
                </div>
              </div>

              <div v-show="isExtensionsOpen(stepIndex)" class="extensions">
                <h4>Extensions</h4>
                <div
                  v-for="(ext, extIndex) in step.extensions"
                  :key="extIndex"
                  class="extension-card"
                >
                  <div class="extension-header">
                    <span>Extension {{ extIndex + 1 }}</span>
                    <el-tooltip content="Remove extension" placement="top">
                      <el-button
                        type="danger"
                        plain
                        size="small"
                        circle
                        icon="Delete"
                        @click="removeExtension(stepIndex, extIndex)"
                      />
                    </el-tooltip>
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
                      class="extension-step-block"
                    >
                      <div class="step-between extension-step-between">
                        <div class="step-insert">
                          <el-button
                            circle
                            plain
                            size="small"
                            icon="Plus"
                            @click="addExtensionStepAt(stepIndex, extIndex, extStepIndex)"
                          />
                          <span @click="addExtensionStepAt(stepIndex, extIndex, extStepIndex)">
                            Insert step
                          </span>
                        </div>
                      </div>

                      <div class="extension-step">
                        <el-input v-model="extStep.actor" class="actor-hidden" />
                        <el-input
                          v-model="extStep.actionText"
                          placeholder="Action"
                          type="textarea"
                          :autosize="{ minRows: 2 }"
                        />
                        <div class="extension-step-actions">
                          <el-tooltip content="Remove step" placement="top">
                            <el-button
                              type="danger"
                              plain
                              size="small"
                              circle
                              icon="Delete"
                              @click="removeExtensionStep(stepIndex, extIndex, extStepIndex)"
                            />
                          </el-tooltip>
                        </div>
                      </div>
                    </div>
                    <div class="step-between extension-step-between">
                      <div class="step-insert">
                        <el-button
                          circle
                          plain
                          size="small"
                          icon="Plus"
                          @click="addExtensionStepAt(stepIndex, extIndex, ext.steps.length)"
                        />
                        <span @click="addExtensionStepAt(stepIndex, extIndex, ext.steps.length)">
                          Insert step
                        </span>
                      </div>
                    </div>
                  </div>
                  <el-button type="primary" plain size="small" @click="addExtension(stepIndex)">
                    Add extension
                  </el-button>
                </div>
              </div>
              <div class="step-between">
                <div class="step-insert">
                  <el-button
                    circle
                    plain
                    size="small"
                    icon="Plus"
                    @click="addStepAt(stepIndex + 1)"
                  />
                  <span @click="addStepAt(stepIndex + 1)">Insert step</span>
                </div>
              </div>
            </div>

            <div class="step-between step-between--bottom">
              <div class="step-insert">
                <el-button
                  circle
                  plain
                  size="small"
                  icon="Plus"
                  @click="addStepAt(currentUseCase.mainSteps.length)"
                />
                <span @click="addStepAt(currentUseCase.mainSteps.length)">Insert step</span>
              </div>
            </div>
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import type { Student } from '@/apis/student/types'
import { PRIORITIES, type RequirementArtifact, type UseCase } from '@/apis/ram/types'
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

// We maintain a separate list of use cases for the sidebar to allow for fast searching and filtering without affecting the main use case data. The sidebar only loads basic artifact info, while the main content loads full use case details on demand.
const useCases = ref<RequirementArtifact[]>([])
const filteredUseCases = ref<RequirementArtifact[]>([])

// selectedUseCaseId tracks which use case is currently selected and displayed in the main content area. When a use case is selected, we load its full details to show in the form.
const selectedUseCaseId = ref<number | null>(null)
const isNew = ref(false)

const stakeholderOptions = ref<RequirementArtifact[]>([])

// currentUseCase holds the full details of the currently selected use case for editing.
const currentUseCase = ref<UseCase>(createEmptyUseCase())

// editingMap tracks which use cases have been edited in the current session. This is used to show an "Editing" tag in the sidebar and to display how long a use case has been in editing mode.
const editingMap = ref<Record<number, string>>({})

// activeStepIndex tracks which main step is currently active for editing. This allows us to show the step details in the sidebar and manage focus when adding new steps.
const activeStepIndex = ref<number | null>(null)
const actionRefs = ref<Record<number, any>>({})

const editingNotice = computed(() => {
  if (!selectedUseCaseId.value) return ''
  const startedAt = editingMap.value[selectedUseCaseId.value]
  if (!startedAt) return ''
  return `Editing in this session since ${new Date(startedAt).toLocaleTimeString()}`
})

const canSave = computed(() => isNew.value || Boolean(selectedUseCaseId.value))

function isEditing(id?: number) {
  if (!id) return false
  return Boolean(editingMap.value[id])
}

function createEmptyUseCase(): UseCase {
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

// We use searchRequirementArtifacts instead of searchUseCases to populate the sidebar without loading too many use case details at once. The main content will load the full use case data when selected.
async function loadUseCases() {
  if (!teamId.value) return
  try {
    const result = await searchRequirementArtifacts(
      teamId.value,
      { page: 0, size: 200 },
      { type: 'USE_CASE' }
    )
    useCases.value = result.data.content
    filterList()
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to load use cases'
    ElMessage.error(message)
  }
}

// In a use case, actors can be either stakeholders or external interface requirements. For example, a stakeholder could be a user role like "Customer" or "Admin", while an external interface requirement could represent an external system that interacts with the use case.
async function loadStakeholders() {
  if (!teamId.value) return
  try {
    const result = await searchRequirementArtifacts(teamId.value, { page: 0, size: 500 }, {})
    stakeholderOptions.value = result.data.content.filter((artifact) =>
      ['STAKEHOLDER', 'EXTERNAL_INTERFACE_REQUIREMENT'].includes(artifact.type)
    )
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to load actors'
    ElMessage.error(message)
  }
}

function filterList() {
  const query = searchText.value.trim().toLowerCase()
  if (!query) {
    filteredUseCases.value = [...useCases.value]
    return
  }
  filteredUseCases.value = useCases.value.filter((item) => item.title.toLowerCase().includes(query))
}

// When a use case is selected from the sidebar, we load its full details to display in the main content area. This allows us to keep the sidebar list fast and responsive, even if there are many use cases.
async function handleSelect(id: string) {
  if (!teamId.value) return
  selectedUseCaseId.value = Number(id)
  isNew.value = false
  try {
    const result = await getUseCaseById(teamId.value, selectedUseCaseId.value)
    currentUseCase.value = normalizeUseCase(result.data)
    markEditing(selectedUseCaseId.value)
    activeStepIndex.value = null
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to load use case'
    ElMessage.error(message)
  }
}

// When creating a new use case, we clear the selectedUseCaseId and set isNew to true. We also initialize currentUseCase with an empty use case object. This allows the form to be used for both creating new use cases and editing existing ones.
function createNew() {
  selectedUseCaseId.value = null
  isNew.value = true
  currentUseCase.value = createEmptyUseCase()
  activeStepIndex.value = null
}

// This function ensures that all optional fields in the use case have default values. This prevents issues with undefined values when rendering the form and allows us to work with a consistent data structure.
function normalizeUseCase(useCase: UseCase): UseCase {
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

// Steps are a core part of the use case, and users often need to add new steps in between existing ones. The addStepAt function allows us to insert a new step at any position in the mainSteps array. After adding a step, we also set it as the active step and focus the action input for a smooth editing experience.
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

// Extensions allow us to capture alternate flows and exceptions in the use case. Each extension has its own set of steps that can be edited independently from the main steps. The addExtension function adds a new extension to a specific step, while removeExtension deletes an existing extension. We also have functions to manage the steps within each extension.
function addExtension(stepIndex: number) {
  const step = currentUseCase.value.mainSteps[stepIndex] // Get the main step at the specified index
  step!.extensions.push({ conditionText: '', kind: 'EXCEPTION', exit: 'RESUME', steps: [] })
  openExtensions(stepIndex)
}

function removeExtension(stepIndex: number, extIndex: number) {
  const step = currentUseCase.value.mainSteps[stepIndex]
  step!.extensions.splice(extIndex, 1)
  if (!step!.extensions.length) closeExtensions(stepIndex)
}

// Similar to main steps, we want to allow users to insert new steps within an extension flow. The addExtensionStepAt function inserts a new step at a specified index within the extension's steps array, while removeExtensionStep deletes a step from the extension.
function addExtensionStepAt(stepIndex: number, extIndex: number, insertIndex: number) {
  const extension = currentUseCase.value.mainSteps[stepIndex]!.extensions[extIndex]
  extension!.steps.splice(insertIndex, 0, { actor: '', actionText: '' })
}

function removeExtensionStep(stepIndex: number, extIndex: number, extStepIndex: number) {
  const extension = currentUseCase.value.mainSteps[stepIndex]!.extensions[extIndex]
  extension!.steps.splice(extStepIndex, 1)
}

// We use openExtensionsMap to track which steps have their extensions currently open in the UI. This allows us to toggle the visibility of the extensions section for each step independently, providing a cleaner editing experience when there are many steps with extensions.
// An example: if openExtensionsMap[2] is true, it means that the extensions for the step at index 2 are currently visible in the UI. If it's false or undefined, the extensions for that step are hidden.
const openExtensionsMap = ref<Record<number, boolean>>({})

// This function checks if the extensions for a given step index are currently open. It returns a boolean value based on the state stored in openExtensionsMap.
function isExtensionsOpen(stepIndex: number) {
  return Boolean(openExtensionsMap.value[stepIndex])
}

// This function toggles the open/closed state of the extensions for a specific step index. It updates the openExtensionsMap by inverting the current value for the given step index.
function toggleExtensions(stepIndex: number) {
  openExtensionsMap.value = {
    ...openExtensionsMap.value,
    [stepIndex]: !openExtensionsMap.value[stepIndex]
  }
}

// These functions allow us to explicitly open or close the extensions for a specific step index. They update the openExtensionsMap accordingly, which in turn controls the visibility of the extensions section in the UI.
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

// The saveUseCase function handles both creating new use cases and updating existing ones. It first validates the form data, then constructs the payload for the API request. For existing use cases, it sends an update request, while for new use cases, it sends a create request. After saving, it reloads the list of use cases to reflect any changes and shows a success message. If there's an error during saving, it displays an error message with details.
async function saveUseCase() {
  if (!teamId.value) return
  if (!validateUseCase()) return

  saving.value = true
  try {
    // Currently, we leave the step.actor empty.
    // TODO: In the future, we need to identify the actor for each step, which can be either a stakeholder or an external interface requirement. For now, we will set it to 'N/A' to indicate that it's not applicable or not yet defined.
    // Should be a very simple logic, scan the step text for the first mention of any primary and secondary actor, and set that as the actor for the step.
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
      }))
    }

    const result =
      isNew.value || !currentUseCase.value.id
        ? await createUseCase(teamId.value, payload)
        : await updateUseCase(teamId.value, currentUseCase.value.id, payload)

    currentUseCase.value = normalizeUseCase(result.data)
    selectedUseCaseId.value = result.data.id || null
    isNew.value = false
    await loadUseCases()
    if (selectedUseCaseId.value) markEditing(selectedUseCaseId.value)
    ElMessage.success('Use case saved')
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to save use case'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

async function ensureUseCasesLoaded() {
  if (!teamId.value) return
  loading.value = true
  try {
    await Promise.all([loadUseCases(), loadStakeholders()])
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await ensureUseCasesLoaded()
})

watch(
  () => teamId.value,
  async (value, previous) => {
    if (!value || value === previous) return
    await ensureUseCasesLoaded()
  }
)
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

.step-insert span {
  cursor: pointer;
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

.extension-step-between {
  min-height: 22px;
}

.step-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #f9fafb;
}

.step-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  background: #fffbeb;
  border-left: 4px solid #f59e0b;
  margin-left: 12px;
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
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.extension-step :deep(.el-textarea) {
  flex: 1;
}

.extension-step :deep(.el-textarea__inner) {
  width: 100%;
}

.extension-step-actions {
  display: flex;
  align-items: flex-start;
  padding-top: 6px;
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
