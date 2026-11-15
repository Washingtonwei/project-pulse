<template>
  <div class="conditions">
    <div
      v-for="(condition, index) in conditions"
      :key="rowKey(condition, index)"
      class="condition-row"
    >
      <div class="condition-line">
        <!-- The display number is this condition's position in this use case; the artifact key is its own. -->
        <span class="condition-number">{{ numberPrefix }}-{{ index + 1 }}</span>

        <!-- A row just added: pick one of the team's conditions, or type a new one. -->
        <el-select
          v-if="condition.choosing"
          class="condition-picker"
          filterable
          allow-create
          default-first-option
          :placeholder="`Choose one of the team's ${label}s, or type a new one`"
          :disabled="!canEdit"
          @change="(value: number | string) => choose(index, value)"
        >
          <el-option
            v-for="option in availableOptions"
            :key="option.id"
            :label="optionLabel(option)"
            :value="option.id as number"
          />
        </el-select>

        <!-- A chosen row. Read-only when shared, or when picked in this session (see isReadOnly). -->
        <el-input
          v-else
          v-model="condition.condition"
          placeholder="Condition"
          :disabled="!canEdit || isReadOnly(condition)"
        />

        <span v-if="condition.artifactKey" class="condition-key">{{ condition.artifactKey }}</span>

        <div class="condition-actions">
          <el-button plain :disabled="!canEdit || index === 0" @click="move(index, -1)"
            >Up</el-button
          >
          <el-button
            plain
            :disabled="!canEdit || index === conditions.length - 1"
            @click="move(index, 1)"
            >Down</el-button
          >
          <el-button type="danger" plain :disabled="!canEdit" @click="remove(index)"
            >Remove</el-button
          >
        </div>
      </div>

      <!-- Extension 6b1: a shared condition is read-only here and says which other use cases reference it. -->
      <div v-if="isShared(condition)" class="condition-note">
        Also used by
        <span v-for="(other, otherIndex) in condition.otherUseCases" :key="other.id">
          {{ other.artifactKey ? `${other.artifactKey}: ` : '' }}{{ other.title
          }}{{ otherIndex < (condition.otherUseCases?.length ?? 0) - 1 ? ', ' : '' }} </span
        >. Its wording can't be changed from this use case.
        <!-- Extension 6b3: replace it, at the same position, with a new condition of the same wording. -->
        <el-button link type="primary" :disabled="!canEdit" @click="makeSeparateCopy(index)">
          Make a separate copy
        </el-button>
      </div>
      <div v-else-if="condition.pickedInSession" class="condition-note">
        Picked from the team's {{ label }}s. Save to see whether other use cases share it before
        changing its wording.
      </div>
    </div>

    <el-button type="primary" plain :disabled="!canEdit" @click="add">Add {{ label }}</el-button>
  </div>
</template>

<script setup lang="ts">
// The preconditions or postconditions of one use case (UC-DOC-create-use-case, UC-DOC-edit-use-case). Each row is
// an ordered reference to a team condition: picked from the team's existing conditions, or typed as a new one. A
// condition that other use cases also reference can't be reworded here (BR-shared-condition-wording); the server
// enforces that too, and this list only keeps the editor from offering it.
import { computed } from 'vue'
import type { Condition, RequirementArtifact } from '@/apis/ram/types'

const props = defineProps<{
  kind: 'PRECONDITION' | 'POSTCONDITION'
  // The team's conditions of this kind, offered by the picker.
  options: RequirementArtifact[]
  canEdit: boolean
}>()

// The use case's list. It may be undefined before the use case is loaded, which reads as empty.
const model = defineModel<Condition[] | undefined>({ required: true })
const conditions = computed<Condition[]>({
  get: () => model.value ?? [],
  set: (rows) => {
    model.value = rows
  }
})

const label = computed(() => (props.kind === 'PRECONDITION' ? 'precondition' : 'postcondition'))
const numberPrefix = computed(() => (props.kind === 'PRECONDITION' ? 'PRE' : 'POST'))

// The picker hides conditions already in this list, since the server refuses a repeated one.
const availableOptions = computed(() => {
  const used = new Set(
    conditions.value.map((condition) => condition.id).filter((id): id is number => id != null)
  )
  return props.options.filter((option) => option.id == null || !used.has(option.id))
})

// A row's key must follow the row, not its position: keyed by position, moving a new row would leave its half-used
// picker behind at the old position. A row with no id yet gets a client-side key of its own.
let nextClientKey = 0
function newClientKey() {
  nextClientKey += 1
  return `new-${nextClientKey}`
}

function rowKey(condition: Condition, index: number) {
  if (condition.id != null) return `id-${condition.id}`
  return condition.clientKey ?? `new-at-${index}`
}

function optionLabel(option: RequirementArtifact) {
  // A condition's wording is its content; it has no title of its own.
  return option.artifactKey ? `${option.artifactKey}: ${option.content}` : (option.content ?? '')
}

function isShared(condition: Condition) {
  return (condition.otherUseCases?.length ?? 0) > 0
}

// A row picked in this session stays read-only until the next save: the search the picker uses doesn't say whether
// the condition is shared, and only the save response does.
function isReadOnly(condition: Condition) {
  return isShared(condition) || Boolean(condition.pickedInSession)
}

function add() {
  conditions.value = [
    ...conditions.value,
    { condition: '', type: props.kind, choosing: true, clientKey: newClientKey() }
  ]
}

// The picker's value is a number when an existing condition was chosen, and the typed text when a new one was made.
function choose(index: number, value: number | string) {
  const rows = [...conditions.value]
  if (typeof value === 'number') {
    const option = props.options.find((candidate) => candidate.id === value)
    if (!option) return
    rows[index] = {
      id: option.id,
      artifactKey: option.artifactKey,
      condition: option.content ?? '',
      type: props.kind,
      pickedInSession: true
    }
  } else {
    rows[index] = { condition: value, type: props.kind, clientKey: newClientKey() }
  }
  conditions.value = rows
}

function move(index: number, offset: number) {
  const target = index + offset
  if (target < 0 || target >= conditions.value.length) return
  const rows = [...conditions.value]
  const [row] = rows.splice(index, 1)
  rows.splice(target, 0, row as Condition)
  conditions.value = rows
}

function remove(index: number) {
  // Removing only drops this use case's link; the condition stays for the team's other use cases.
  conditions.value = conditions.value.filter((_, rowIndex) => rowIndex !== index)
}

function makeSeparateCopy(index: number) {
  const rows = [...conditions.value]
  const shared = rows[index]
  if (!shared) return
  // No id: the server stores this as a new condition with the same wording, which only this use case references.
  rows[index] = { condition: shared.condition, type: props.kind, clientKey: newClientKey() }
  conditions.value = rows
}
</script>

<style scoped>
.conditions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.condition-line {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 8px;
  align-items: center;
}

.condition-number {
  font-weight: 600;
  min-width: 56px;
}

.condition-picker {
  width: 100%;
}

.condition-key {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.condition-actions {
  display: flex;
  gap: 4px;
}

.condition-note {
  margin: 4px 0 0 64px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

@media (max-width: 768px) {
  .condition-line {
    grid-template-columns: 1fr;
  }

  .condition-note {
    margin-left: 0;
  }
}
</style>
