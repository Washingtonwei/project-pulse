<template>
  <div class="ram-use-cases">
    <el-page-header content="Use Cases" @back="goBack" />

    <el-alert
      v-if="!hasTeam"
      type="warning"
      show-icon
      :title="isInstructor ? 'No team selected' : 'No team assigned'"
      :description="
        isInstructor
          ? 'Select a team to view and manage use cases.'
          : 'Ask your instructor to assign you to a team before authoring use cases.'
      "
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
                <el-tag v-if="isLockedInUi(item.id)" size="small" type="warning">Locked</el-tag>
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
            </div>
            <div class="content-actions">
              <el-button
                v-if="!isNew && selectedUseCaseId && canLock"
                type="primary"
                plain
                :loading="locking"
                @click="lockSelectedUseCase"
              >
                Lock
              </el-button>
              <el-button
                v-if="!isNew && selectedUseCaseId && canUnlock"
                type="warning"
                plain
                :loading="unlocking"
                @click="unlockSelectedUseCase"
              >
                Unlock
              </el-button>
              <el-button type="success" :loading="saving" :disabled="!canSave" @click="saveUseCase">
                Save
              </el-button>
            </div>
          </div>
        </template>

        <el-empty
          v-if="!isNew && !selectedUseCaseId"
          description="Select a use case on the left or create a new one."
        />

        <el-form v-else label-width="140px" class="usecase-form">
          <!-- Created By and Date Created come from the authorship metadata, never from the student
               (UC-DOC-view-use-case, FR-HIS-authorship-metadata). -->
          <div v-if="!isNew && currentUseCase.createdAt" class="authorship">
            Created by {{ currentUseCase.createdBy?.name || 'unknown' }} on {{ formatTimestamp(currentUseCase.createdAt) }}
          </div>
          <el-alert v-if="lockInfo?.locked && !isNew" type="warning" show-icon class="lock-alert">
            <template #title>
              Use case locked by {{ lockInfo.lockedBy?.name || 'another user' }}
            </template>
            <template #default>
              <div>Expires at: {{ formatTimestamp(lockInfo.expiresAt) }}</div>
              <div v-if="lockInfo.reason">Reason: {{ lockInfo.reason }}</div>
            </template>
          </el-alert>
          <el-form-item label="Title">
            <el-input v-model="currentUseCase.title" placeholder="Use case title" :disabled="!canEdit" />
          </el-form-item>
          <el-form-item label="Description">
            <el-input
              v-model="currentUseCase.description"
              type="textarea"
              :autosize="{ minRows: 3 }"
              :disabled="!canEdit"
            />
          </el-form-item>
          <el-form-item label="Trigger">
            <el-input
              v-model="currentUseCase.trigger"
              type="textarea"
              :autosize="{ minRows: 2 }"
              :disabled="!canEdit"
            />
          </el-form-item>
          <el-form-item label="Primary Actor">
            <el-select
              ref="primaryActorSelect"
              v-model="currentUseCase.primaryActorId"
              filterable
              :filter-method="(query: string) => (actorQuery.primary = query)"
              placeholder="Select primary actor"
              :disabled="!canEdit"
              @visible-change="(open: boolean) => !open && (actorQuery.primary = '')"
            >
              <el-option
                v-for="actor in matchingActors(stakeholderOptions, actorQuery.primary)"
                :key="actor.id"
                :label="formatActorLabel(actor)"
                :value="actor.id"
              />
              <!-- Extension 3a / 6c: below the matches, so the student sees them before she creates one. -->
              <template v-if="canEdit && offersCreate(actorQuery.primary)" #footer>
                <el-button link type="primary" @mousedown.prevent @click="openActorDialog('primary')">
                  Create "{{ actorQuery.primary.trim() }}"...
                </el-button>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item label="Secondary Actors">
            <el-select
              ref="secondaryActorSelect"
              v-model="currentUseCase.secondaryActorIds"
              multiple
              filterable
              :filter-method="(query: string) => (actorQuery.secondary = query)"
              placeholder="Select secondary actors"
              :disabled="!canEdit"
              @visible-change="(open: boolean) => !open && (actorQuery.secondary = '')"
            >
              <el-option
                v-for="actor in matchingActors(secondaryActorOptions, actorQuery.secondary)"
                :key="actor.id"
                :label="formatActorLabel(actor)"
                :value="actor.id"
              />
              <template v-if="canEdit && offersCreate(actorQuery.secondary)" #footer>
                <el-button link type="primary" @mousedown.prevent @click="openActorDialog('secondary')">
                  Create "{{ actorQuery.secondary.trim() }}"...
                </el-button>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item label="Priority">
            <el-select v-model="currentUseCase.priority" placeholder="Priority" :disabled="!canEdit">
              <el-option
                v-for="priority in PRIORITIES"
                :key="priority"
                :label="priority"
                :value="priority"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Frequency of Use">
            <el-input
              v-model="currentUseCase.frequencyOfUse"
              maxlength="255"
              show-word-limit
              placeholder="For example, about 50 times a day during the semester"
              :disabled="!canEdit"
            />
          </el-form-item>
          <el-form-item label="Notes">
            <el-input
              v-model="currentUseCase.notes"
              type="textarea"
              :autosize="{ minRows: 2 }"
              :disabled="!canEdit"
            />
          </el-form-item>
        </el-form>

        <!-- Extensions 3a and 6c: a missing actor is created through UC-ART-create-artifact, apart from the use case
             save. The primary actor is left out of the secondary picker's duplicate list (BR-use-case-actor-types). -->
        <RequirementArtifactCreateDialog
          v-model="actorDialog.visible"
          :team-id="effectiveTeamId"
          :types="ACTOR_TYPES"
          :initial-title="actorDialog.title"
          :exclude-ids="
            actorDialog.role === 'secondary' && currentUseCase.primaryActorId
              ? [currentUseCase.primaryActorId]
              : []
          "
          @created="selectActor"
          @chosen="selectActor"
        />

        <template v-if="isNew || selectedUseCaseId">
          <el-divider>Preconditions</el-divider>
          <UseCaseConditionList
            v-model="currentUseCase.preconditions"
            kind="PRECONDITION"
            :options="preconditionOptions"
            :can-edit="canEdit"
          />

          <el-divider>Postconditions</el-divider>
          <UseCaseConditionList
            v-model="currentUseCase.postconditions"
            kind="POSTCONDITION"
            :options="postconditionOptions"
            :can-edit="canEdit"
          />

          <el-divider>Main Steps</el-divider>
          <div class="steps">
            <div class="step-between step-between--top">
              <div class="step-insert">
                <el-button circle plain size="small" icon="Plus" :disabled="!canEdit" @click="addStepAt(0)" />
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
                        :disabled="!canEdit"
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
                        :disabled="!canEdit"
                        @click="removeStep(stepIndex)"
                      />
                    </el-tooltip>
                  </div>
                </div>
                <div class="step-body">
                  <el-input v-model="step.actor" class="actor-hidden" :disabled="!canEdit" />
                  <el-input
                    :ref="(el: any) => setActionRef(el, stepIndex)"
                    v-model="step.actionText"
                    placeholder="Action"
                    type="textarea"
                    :autosize="{ minRows: 2 }"
                    :disabled="!canEdit"
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
                        :disabled="!canEdit"
                        @click="removeExtension(stepIndex, extIndex)"
                      />
                    </el-tooltip>
                  </div>
                  <el-input v-model="ext.conditionText" placeholder="Condition" :disabled="!canEdit" />
                  <div class="extension-meta">
                    <el-select v-model="ext.kind" placeholder="Kind" :disabled="!canEdit">
                      <el-option label="Alternate" value="ALTERNATE" />
                      <el-option label="Exception" value="EXCEPTION" />
                    </el-select>
                    <el-select v-model="ext.exit" placeholder="Exit" :disabled="!canEdit">
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
                            :disabled="!canEdit"
                            @click="addExtensionStepAt(stepIndex, extIndex, extStepIndex)"
                          />
                          <span @click="addExtensionStepAt(stepIndex, extIndex, extStepIndex)">
                            Insert step
                          </span>
                        </div>
                      </div>

                      <div class="extension-step">
                        <el-input v-model="extStep.actor" class="actor-hidden" :disabled="!canEdit" />
                        <el-input
                          v-model="extStep.actionText"
                          placeholder="Action"
                          type="textarea"
                          :autosize="{ minRows: 2 }"
                          :disabled="!canEdit"
                        />
                        <div class="extension-step-actions">
                          <el-tooltip content="Remove step" placement="top">
                            <el-button
                              type="danger"
                              plain
                              size="small"
                              circle
                              icon="Delete"
                              :disabled="!canEdit"
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
                          :disabled="!canEdit"
                          @click="addExtensionStepAt(stepIndex, extIndex, ext.steps.length)"
                        />
                        <span @click="addExtensionStepAt(stepIndex, extIndex, ext.steps.length)">
                          Insert step
                        </span>
                      </div>
                    </div>
                  </div>
                  <el-button type="primary" plain size="small" :disabled="!canEdit" @click="addExtension(stepIndex)">
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
                    :disabled="!canEdit"
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
                  :disabled="!canEdit"
                  @click="addStepAt(currentUseCase.mainSteps.length)"
                />
                <span @click="addStepAt(currentUseCase.mainSteps.length)">Insert step</span>
              </div>
            </div>
          </div>

          <!-- Business Rules (BR-use-case-business-rules): the team's rules this use case must conform to, saved as
               CONSTRAINS links from each rule. The picker shows matches first, then Create (extensions 3b and 6d). -->
          <el-divider>Business Rules</el-divider>
          <el-select
            ref="businessRuleSelect"
            v-model="currentUseCase.businessRuleIds"
            multiple
            filterable
            :filter-method="(query: string) => (ruleQuery = query)"
            placeholder="Select business rules"
            class="business-rule-select"
            :disabled="!canEdit"
            @visible-change="(open: boolean) => !open && (ruleQuery = '')"
            @change="sortBusinessRuleIds"
          >
            <el-option
              v-for="rule in matchingRules(ruleQuery)"
              :key="rule.id"
              :label="formatActorLabel(rule)"
              :value="rule.id"
            />
            <template v-if="canEdit && offersRuleCreate(ruleQuery)" #footer>
              <el-button link type="primary" @mousedown.prevent @click="openRuleDialog">
                Create "{{ ruleQuery.trim() }}"...
              </el-button>
            </template>
          </el-select>

          <!-- Extensions 3b and 6d: a missing rule is created through UC-ART-create-artifact, apart from the use case
               save; its type is always business rule. The rules already chosen are left out of the duplicate list. -->
          <RequirementArtifactCreateDialog
            v-model="ruleDialog.visible"
            :team-id="effectiveTeamId"
            :types="RULE_TYPES"
            :initial-title="ruleDialog.title"
            :exclude-ids="currentUseCase.businessRuleIds"
            @created="selectRule"
            @chosen="selectRule"
          />

          <!-- The template's rich text fields (docs/design/doc.md, "The template's text fields"), all optional.
               Assumptions are only this use case's own; a project-wide one is cited by its artifact key. -->
          <el-divider>Associated Information</el-divider>
          <RichTextField v-model="currentUseCase.associatedInformation" :editable="canEdit" />

          <el-divider>Assumptions</el-divider>
          <RichTextField v-model="currentUseCase.assumptions" :editable="canEdit" />

          <el-divider>Open Issues</el-divider>
          <RichTextField v-model="currentUseCase.openIssues" :editable="canEdit" />
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import type { Student } from '@/apis/student/types'
import { PRIORITIES, type Condition, type RequirementArtifact, type UseCase } from '@/apis/ram/types'
import UseCaseConditionList from '@/components/UseCaseConditionList.vue'
import RequirementArtifactCreateDialog from '@/components/RequirementArtifactCreateDialog.vue'
import RichTextField from '@/components/RichTextField.vue'
import {
  searchRequirementArtifacts,
  getUseCaseById,
  createUseCase,
  updateUseCase,
  getUseCaseLock,
  lockUseCase,
  unlockUseCase
} from '@/apis/ram'

const route = useRoute()
const router = useRouter()
const userInfoStore = useUserInfoStore()

const isInstructor = computed(() => userInfoStore.isInstructor)
const studentTeamId = computed(() => (userInfoStore.userInfo as Student | null)?.teamId ?? null)
const routeTeamId = computed(() => {
  const raw = route.query.teamId
  const value = Array.isArray(raw) ? raw[0] : raw
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
})
const effectiveTeamId = computed(() =>
  isInstructor.value ? routeTeamId.value : studentTeamId.value
)
const currentUserId = computed(() => userInfoStore.userInfo?.id ?? null)
const hasTeam = computed(() => Boolean(effectiveTeamId.value))
const ramQuery = computed(() =>
  isInstructor.value && effectiveTeamId.value
    ? { teamId: String(effectiveTeamId.value) }
    : undefined
)

const loading = ref(false)
const saving = ref(false)
const locking = ref(false)
const unlocking = ref(false)
const searchText = ref('')

// We maintain a separate list of use cases for the sidebar to allow for fast searching and filtering without affecting the main use case data. The sidebar only loads basic artifact info, while the main content loads full use case details on demand.
const useCases = ref<RequirementArtifact[]>([])
const filteredUseCases = ref<RequirementArtifact[]>([])

// selectedUseCaseId tracks which use case is currently selected and displayed in the main content area. When a use case is selected, we load its full details to show in the form.
const selectedUseCaseId = ref<number | null>(null)
const isNew = ref(false)

const stakeholderOptions = ref<RequirementArtifact[]>([])
// The team's existing conditions, offered by the condition pickers so that one isn't entered twice.
const preconditionOptions = ref<RequirementArtifact[]>([])
const postconditionOptions = ref<RequirementArtifact[]>([])

// currentUseCase holds the full details of the currently selected use case for editing.
const currentUseCase = ref<UseCase>(createEmptyUseCase())

// activeStepIndex tracks which main step is currently active for editing. This allows us to show the step details in the sidebar and manage focus when adding new steps.
const activeStepIndex = ref<number | null>(null)
const actionRefs = ref<Record<number, any>>({})

const lockInfo = computed(() => currentUseCase.value.lock || null)
const lockedByMe = computed(
  () =>
    Boolean(
      lockInfo.value?.locked && currentUserId.value && lockInfo.value.lockedBy?.id === currentUserId.value
    )
)
const canLock = computed(() => Boolean(!isNew.value && selectedUseCaseId.value && !lockInfo.value?.locked))
const canUnlock = computed(
  () => Boolean(!isNew.value && selectedUseCaseId.value && lockInfo.value?.locked && (lockedByMe.value || isInstructor.value))
)
const canEdit = computed(() => Boolean(isNew.value || lockedByMe.value))
const canSave = computed(() => Boolean(isNew.value || (selectedUseCaseId.value && lockedByMe.value)))

function formatTimestamp(value?: string | null) {
  if (!value) return 'unknown'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}

function isLockedInUi(id?: number) {
  if (!id || !selectedUseCaseId.value) return false
  return selectedUseCaseId.value === id && Boolean(lockInfo.value?.locked)
}

function createEmptyUseCase(): UseCase {
  return {
    title: '',
    description: '',
    teamId: effectiveTeamId.value || 0,
    primaryActorId: 0,
    secondaryActorIds: [],
    businessRuleIds: [],
    trigger: '',
    frequencyOfUse: '',
    associatedInformation: '',
    assumptions: '',
    openIssues: '',
    preconditions: [],
    postconditions: [],
    mainSteps: [],
    priority: 'MEDIUM',
    notes: '',
    version: null,
    lock: null
  }
}

function goBack() {
  router.push({ name: 'ram-documents', query: ramQuery.value })
}

// We use searchRequirementArtifacts instead of searchUseCases to populate the sidebar without loading too many use case details at once. The main content will load the full use case data when selected.
async function loadUseCases() {
  if (!effectiveTeamId.value) return
  try {
    const result = await searchRequirementArtifacts(
      effectiveTeamId.value,
      { page: 0, size: 200 },
      { type: 'USE_CASE' }
    )
    useCases.value = result.data.content
    filterList()
  } catch {
  }
}

// A use case's actors are the team's stakeholders and external systems (BR-use-case-actor-types). A stakeholder is a
// person or role, such as "Customer"; an external system is a non-human participant, such as an email or LLM service.
// The server refuses any other artifact type, so this filter only keeps the picker from offering one.
// An actor is never both the primary actor and a secondary actor of one use case (BR-use-case-actor-types), so the
// secondary picker leaves out the chosen primary actor; the server refuses the pair with a 400 as the backstop.
const secondaryActorOptions = computed(() =>
  stakeholderOptions.value.filter((actor) => actor.id !== currentUseCase.value.primaryActorId)
)

// Choosing as primary actor one already picked as secondary takes it out of the secondary actors.
watch(
  () => currentUseCase.value.primaryActorId,
  (primaryActorId) => {
    const secondaryActorIds = currentUseCase.value.secondaryActorIds
    if (secondaryActorIds?.includes(primaryActorId)) {
      currentUseCase.value.secondaryActorIds = secondaryActorIds.filter((id) => id !== primaryActorId)
    }
  }
)

// The two artifact types an actor may have (BR-use-case-actor-types), in the order the create dialog offers them.
const ACTOR_TYPES = ['STAKEHOLDER', 'EXTERNAL_SYSTEM']

// What the student has typed into each actor picker. The pickers filter by it, and their Create footers prefill
// the new actor's title from it (extensions 3a and 6c). Cleared when a picker closes.
const actorQuery = ref({ primary: '', secondary: '' })

// The actor create dialog, and which picker opened it, so the new actor lands in that role.
const actorDialog = ref<{ visible: boolean; role: 'primary' | 'secondary'; title: string }>({
  visible: false,
  role: 'primary',
  title: ''
})

// The actors whose label contains the query, ignoring case. With no query, every actor is offered.
function matchingActors(actors: RequirementArtifact[], query: string) {
  const needle = query.trim().toLowerCase()
  if (!needle) return actors
  return actors.filter((actor) => formatActorLabel(actor).toLowerCase().includes(needle))
}

// The two pickers, so the one that opened the dialog can be closed behind it.
const primaryActorSelect = ref<{ blur: () => void }>()
const secondaryActorSelect = ref<{ blur: () => void }>()

// Create is offered for a typed query unless it is exactly an existing actor's title or key, ignoring case: then the
// student has found the actor, and a new one would be a duplicate (or a stakeholder titled "STK-1").
function offersCreate(query: string) {
  const needle = query.trim().toLowerCase()
  if (!needle) return false
  return !stakeholderOptions.value.some(
    (actor) => actor.title.trim().toLowerCase() === needle || actor.artifactKey?.toLowerCase() === needle
  )
}

function openActorDialog(role: 'primary' | 'secondary') {
  actorDialog.value = { visible: true, role, title: actorQuery.value[role].trim() }
  ;(role === 'primary' ? primaryActorSelect : secondaryActorSelect).value?.blur()
}

// Extensions 3a2 and 6c2: the new actor, or the existing one picked from the duplicate warning, is selected in the
// role whose picker opened the dialog. It joins the options first, so the picker shows its key and title. A new
// primary actor leaves the secondary actors through the watch on primaryActorId, and the secondary picker never adds
// the primary actor, so the form never names one actor in both roles.
function selectActor(actor: RequirementArtifact) {
  if (actor.id == null) return
  const id = actor.id
  if (!stakeholderOptions.value.some((option) => option.id === id)) {
    stakeholderOptions.value = [...stakeholderOptions.value, actor]
  }
  if (actorDialog.value.role === 'primary') {
    currentUseCase.value.primaryActorId = id
  } else {
    const secondaryActorIds = currentUseCase.value.secondaryActorIds ?? []
    if (id !== currentUseCase.value.primaryActorId && !secondaryActorIds.includes(id)) {
      currentUseCase.value.secondaryActorIds = [...secondaryActorIds, id]
    }
  }
  actorQuery.value[actorDialog.value.role] = ''
}

async function loadStakeholders() {
  if (!effectiveTeamId.value) return
  try {
    const result = await searchRequirementArtifacts(effectiveTeamId.value, { page: 0, size: 500 }, {})
    stakeholderOptions.value = result.data.content.filter((artifact) =>
      ACTOR_TYPES.includes(artifact.type)
    )
  } catch {
  }
}

// ---- Business rules (BR-use-case-business-rules; extensions 3b and 6d) ----

// The team's business rules, offered by the business rule picker, in artifact key order.
const businessRuleOptions = ref<RequirementArtifact[]>([])

// The one artifact type a use case's business rule has; the create dialog then hides its type choice.
const RULE_TYPES = ['BUSINESS_RULE']

// What the student has typed into the business rule picker; cleared when it closes.
const ruleQuery = ref('')

const ruleDialog = ref<{ visible: boolean; title: string }>({ visible: false, title: '' })

const businessRuleSelect = ref<{ blur: () => void }>()

// The number in an artifact key, so that RULE-2 sorts before RULE-10.
function keyNumber(artifact: RequirementArtifact | undefined) {
  const match = artifact?.artifactKey?.match(/(\d+)$/)
  return match ? Number(match[1]) : Number.MAX_SAFE_INTEGER
}

// The rules whose key or title contains the query, ignoring case ("a rule's artifact key or wording").
function matchingRules(query: string) {
  const needle = query.trim().toLowerCase()
  if (!needle) return businessRuleOptions.value
  return businessRuleOptions.value.filter((rule) => formatActorLabel(rule).toLowerCase().includes(needle))
}

// Create is offered while a query is typed that is not exactly a rule's title or key, as for actors.
function offersRuleCreate(query: string) {
  const needle = query.trim().toLowerCase()
  if (!needle) return false
  return !businessRuleOptions.value.some(
    (rule) => rule.title.trim().toLowerCase() === needle || rule.artifactKey?.toLowerCase() === needle
  )
}

function openRuleDialog() {
  ruleDialog.value = { visible: true, title: ruleQuery.value.trim() }
  businessRuleSelect.value?.blur()
}

// The chosen rules are shown in artifact key order, however they were picked or sent.
function sortedByRuleKey(ids: number[]) {
  const byId = new Map(businessRuleOptions.value.map((rule) => [rule.id, rule]))
  return [...ids].sort((a, b) => keyNumber(byId.get(a)) - keyNumber(byId.get(b)))
}

function sortBusinessRuleIds() {
  currentUseCase.value.businessRuleIds = sortedByRuleKey(currentUseCase.value.businessRuleIds || [])
}

// Extensions 3b2 and 6d2: the new rule, or the existing one picked from the duplicate warning, joins this use case's
// business rules. It joins the options first, so the picker shows its key and title.
function selectRule(rule: RequirementArtifact) {
  if (rule.id == null) return
  const id = rule.id
  if (!businessRuleOptions.value.some((option) => option.id === id)) {
    businessRuleOptions.value = [...businessRuleOptions.value, rule].sort((a, b) => keyNumber(a) - keyNumber(b))
  }
  const ids = currentUseCase.value.businessRuleIds || []
  if (!ids.includes(id)) {
    currentUseCase.value.businessRuleIds = [...ids, id]
    sortBusinessRuleIds()
  }
  ruleQuery.value = ''
}

// The team's rules load in parallel with the use case, so a use case can arrive before its rules' keys are known and
// be sorted on nothing. Sorting again whenever the options change puts the chosen rules in key order once they are.
watch(businessRuleOptions, sortBusinessRuleIds)

async function loadBusinessRules() {
  if (!effectiveTeamId.value) return
  try {
    const result = await searchRequirementArtifacts(effectiveTeamId.value, { page: 0, size: 500 }, { type: 'BUSINESS_RULE' })
    businessRuleOptions.value = [...result.data.content].sort((a, b) => keyNumber(a) - keyNumber(b))
  } catch {
    // The picker then offers no rules; the request interceptor has already shown the error.
  }
}

// Loads the team's conditions of each kind for the pickers. Searched by type, so that only conditions come back.
async function loadConditionOptions() {
  if (!effectiveTeamId.value) return
  try {
    const [preconditions, postconditions] = await Promise.all([
      searchRequirementArtifacts(effectiveTeamId.value, { page: 0, size: 500 }, { type: 'PRECONDITION' }),
      searchRequirementArtifacts(effectiveTeamId.value, { page: 0, size: 500 }, { type: 'POSTCONDITION' })
    ])
    preconditionOptions.value = preconditions.data.content
    postconditionOptions.value = postconditions.data.content
  } catch {
    // The pickers then offer no existing conditions; the request interceptor has already shown the error.
  }
}

// Only what the server reads about a condition: which existing one it is, if any, and its wording.
function conditionPayload(conditions: Condition[] | undefined) {
  return (conditions || []).map((condition) => ({ id: condition.id, condition: condition.condition, type: condition.type }))
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
  if (!effectiveTeamId.value) return
  selectedUseCaseId.value = Number(id)
  isNew.value = false
  try {
    const result = await getUseCaseById(effectiveTeamId.value, selectedUseCaseId.value)
    currentUseCase.value = normalizeUseCase(result.data)
    await refreshUseCaseLock()
    activeStepIndex.value = null
  } catch {
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
// The primary actor is also taken out of the secondary actors: the link routes can leave a use case naming one actor in
// both roles (OI-77), and the secondary picker, which leaves out the primary actor, would then show a bare id.
function normalizeUseCase(useCase: UseCase): UseCase {
  return {
    ...useCase,
    secondaryActorIds: (useCase.secondaryActorIds || []).filter((id) => id !== useCase.primaryActorId),
    // The server sends the rule ids as a set, in no order; the form shows them in artifact key order.
    businessRuleIds: sortedByRuleKey(useCase.businessRuleIds || []),
    // The server returns a field as stored, null included; the form works with ''.
    frequencyOfUse: useCase.frequencyOfUse ?? '',
    associatedInformation: useCase.associatedInformation ?? '',
    assumptions: useCase.assumptions ?? '',
    openIssues: useCase.openIssues ?? '',
    preconditions: useCase.preconditions || [],
    postconditions: useCase.postconditions || [],
    mainSteps: useCase.mainSteps || [],
    version: useCase.version ?? null,
    lock: useCase.lock || null
  }
}

async function refreshUseCaseLock() {
  if (!effectiveTeamId.value || !selectedUseCaseId.value || isNew.value) return
  const result = await getUseCaseLock(effectiveTeamId.value, selectedUseCaseId.value)
  currentUseCase.value = normalizeUseCase({
    ...currentUseCase.value,
    lock: result.data
  })
}

async function lockSelectedUseCase() {
  if (!effectiveTeamId.value || !selectedUseCaseId.value || isNew.value) return
  locking.value = true
  try {
    const result = await lockUseCase(effectiveTeamId.value, selectedUseCaseId.value, {})
    currentUseCase.value = normalizeUseCase({ ...currentUseCase.value, lock: result.data })
    await refreshUseCaseLock()
    ElMessage.success('Use case locked')
  } catch (error: any) {
    await refreshUseCaseLock().catch(() => undefined)
  } finally {
    locking.value = false
  }
}

async function unlockSelectedUseCase() {
  if (!effectiveTeamId.value || !selectedUseCaseId.value || isNew.value) return
  unlocking.value = true
  try {
    await unlockUseCase(effectiveTeamId.value, selectedUseCaseId.value)
    await refreshUseCaseLock()
    ElMessage.success('Use case unlocked')
  } catch {
    await refreshUseCaseLock().catch(() => undefined)
  } finally {
    unlocking.value = false
  }
}

function formatActorLabel(actor: RequirementArtifact) {
  if (!actor) return ''
  const key = actor.artifactKey ? `${actor.artifactKey} — ` : ''
  return `${key}${actor.title}`
}

// Steps are a core part of the use case, and users often need to add new steps in between existing ones. The addStepAt function allows us to insert a new step at any position in the mainSteps array. After adding a step, we also set it as the active step and focus the action input for a smooth editing experience.
function addStepAt(index: number) {
  if (!canEdit.value) return
  currentUseCase.value.mainSteps.splice(index, 0, { actor: '', actionText: '', extensions: [] })
  focusStep(index)
}

function removeStep(index: number) {
  if (!canEdit.value) return
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
  if (!canEdit.value) return
  const step = currentUseCase.value.mainSteps[stepIndex] // Get the main step at the specified index
  step!.extensions.push({ conditionText: '', kind: 'EXCEPTION', exit: 'RESUME', steps: [] })
  openExtensions(stepIndex)
}

function removeExtension(stepIndex: number, extIndex: number) {
  if (!canEdit.value) return
  const step = currentUseCase.value.mainSteps[stepIndex]
  step!.extensions.splice(extIndex, 1)
  if (!step!.extensions.length) closeExtensions(stepIndex)
}

// Similar to main steps, we want to allow users to insert new steps within an extension flow. The addExtensionStepAt function inserts a new step at a specified index within the extension's steps array, while removeExtensionStep deletes a step from the extension.
function addExtensionStepAt(stepIndex: number, extIndex: number, insertIndex: number) {
  if (!canEdit.value) return
  const extension = currentUseCase.value.mainSteps[stepIndex]!.extensions[extIndex]
  extension!.steps.splice(insertIndex, 0, { actor: '', actionText: '' })
}

function removeExtensionStep(stepIndex: number, extIndex: number, extStepIndex: number) {
  if (!canEdit.value) return
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
  const blankCondition = [...(currentUseCase.value.preconditions || []), ...(currentUseCase.value.postconditions || [])]
    .some((condition) => condition.choosing || !condition.condition.trim())
  if (blankCondition) {
    ElMessage.warning('Every precondition and postcondition needs wording')
    return false
  }
  return true
}

// The saveUseCase function handles both creating new use cases and updating existing ones. It first validates the form data, then constructs the payload for the API request. For existing use cases, it sends an update request, while for new use cases, it sends a create request. After saving, it reloads the list of use cases to reflect any changes and shows a success message. If there's an error during saving, it displays an error message with details.
async function saveUseCase() {
  if (!effectiveTeamId.value) return
  if (!validateUseCase()) return
  if (!isNew.value && !lockedByMe.value) {
    ElMessage.warning('Lock this use case before saving changes.')
    return
  }

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
      teamId: effectiveTeamId.value,
      preconditions: conditionPayload(currentUseCase.value.preconditions),
      postconditions: conditionPayload(currentUseCase.value.postconditions),
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
        ? await createUseCase(effectiveTeamId.value, payload)
        : await updateUseCase(effectiveTeamId.value, currentUseCase.value.id, payload)

    currentUseCase.value = normalizeUseCase(result.data)
    selectedUseCaseId.value = result.data.id || null
    isNew.value = false
    // A save may have created conditions, so the pickers reload to offer them.
    await Promise.all([loadUseCases(), loadConditionOptions()])
    ElMessage.success('Use case saved')
  } catch (error: any) {
    if (error?.response?.status === 409 || error?.response?.status === 423) {
      await refreshUseCaseLock()
    }
  } finally {
    saving.value = false
  }
}

async function ensureUseCasesLoaded() {
  if (!effectiveTeamId.value) return
  loading.value = true
  try {
    await Promise.all([loadUseCases(), loadStakeholders(), loadConditionOptions(), loadBusinessRules()])
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await ensureUseCasesLoaded()
})

watch(
  () => effectiveTeamId.value,
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

.content-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.meta {
  font-size: 12px;
  color: #6b7280;
}

.usecase-form {
  margin-bottom: 12px;
}

.lock-alert {
  margin-bottom: 12px;
}

.business-rule-select {
  width: 100%;
}

.authorship {
  margin-bottom: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
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
