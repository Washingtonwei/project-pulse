<template>
  <div class="artifact-key-field" :class="{ compact }">
    <!-- The prefix is the type's and never changes; the student writes the slug or asks for a suggestion. -->
    <el-input
      v-model="slug"
      :size="compact ? 'small' : 'default'"
      :disabled="disabled || !prefix"
      :placeholder="prefix ? 'name-in-lowercase-words' : 'Choose a type first'"
      :maxlength="MAX_SLUG_LENGTH"
      @input="onSlugInput"
    >
      <template #prepend>{{ prefix ? prefix + '-' : '?' }}</template>
      <template #append>
        <el-button
          :loading="suggesting"
          :disabled="disabled || !prefix || !hasSomethingToSuggestFrom"
          @click="suggest"
        >
          Suggest
        </el-button>
      </template>
    </el-input>
    <div class="key-status">
      <span v-if="slug && problem" class="key-error">{{ problem }}</span>
      <span v-else-if="heldByAnother && holder">
        {{ key }} is already used by {{ holder.artifactKey
        }}<template v-if="holder.title">: {{ holder.title }}</template
        >.
        <el-link type="primary" @click="emit('openHolder', holder)">Open it</el-link>
        <template v-if="offersUseCondition">
          or
          <el-link type="primary" @click="emit('useCondition', holder)">use that condition</el-link>
        </template>
      </span>
      <span v-else-if="slug && holder === null && !compact" class="key-free">Available</span>
      <span v-if="suggestNote" class="key-note">{{ suggestNote }}</span>
      <span v-if="renamed" class="key-warning">
        Renaming the key does not update mentions of {{ originalKey }} written into text.
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
// The artifact key field every editor uses (UC-ART-create-artifact, "Artifact key rules"; design:
// docs/design/art.md, "Artifact keys"): the requirement artifact dialogs, the glossary's create and rename dialogs,
// the use case form, and a new condition's row. It shows the type's fixed prefix and a box for the slug, suggests a
// slug only when the student presses Suggest, and tells her whether another artifact of her team already holds the key.
//
// v-model is the full key. For a functional requirement whose student types "export-csv", the parent's model becomes
// "FR-export-csv"; an empty slug makes it "". The availability line asks the server after each pause in typing:
//   "export-csv", nobody holds FR-export-csv  -> "Available"
//   "user-authentication", the seed holds it  -> "FR-user-authentication is already used by FR-user-authentication:
//                                                 User authentication. Open it"
// This answer is a convenience: the create or save checks again on the server (4b, 8c).
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { findArtifactKeyHolder, suggestArtifactKey } from '@/apis/ram'
import type { ArtifactKeyHolder } from '@/apis/ram/types'
import { MAX_SLUG_LENGTH, prefixOf, slugOf, slugProblem } from '@/utils/artifactKeys'

// How long the availability check waits after the last keystroke.
const AVAILABILITY_DELAY_MS = 300

const props = withDefaults(
  defineProps<{
    teamId: number | null | undefined
    // The artifact's type, which sets the prefix; empty while none is chosen.
    type: string
    // What Suggest is based on: a glossary term's term and definition, a use case's name and description, a condition's
    // wording as content.
    title?: string | null
    content?: string | null
    // The artifact's own id when editing, so its current key reads as free rather than held.
    selfId?: number | null
    // The key the artifact had when the editor opened, for the rename reminder (UC-ART-edit-artifact).
    originalKey?: string | null
    // A smaller field for a condition row.
    compact?: boolean
    // A condition row: a holder that is a condition of the same type is offered as "use that condition" (4b1, 7b1).
    offerConditionReuse?: boolean
    // A condition this row must not offer, such as the one a separate copy (6b3) replaces.
    noReuseOf?: number | null
    disabled?: boolean
  }>(),
  {
    title: '',
    content: '',
    selfId: null,
    originalKey: null,
    compact: false,
    offerConditionReuse: false,
    noReuseOf: null,
    disabled: false
  }
)

const emit = defineEmits<{
  openHolder: [holder: ArtifactKeyHolder]
  useCondition: [holder: ArtifactKeyHolder]
}>()

const key = defineModel<string>({ default: '' })

const prefix = computed(() => prefixOf(props.type))
const slug = ref(slugOf(key.value, props.type))
// The holder of the current key: undefined while unknown (not asked yet, or a malformed slug), null when free.
const holder = ref<ArtifactKeyHolder | null | undefined>(undefined)
const suggesting = ref(false)
const suggestNote = ref('')

const problem = computed(() => slugProblem(slug.value))
const hasSomethingToSuggestFrom = computed(
  () => !!(props.title && props.title.trim()) || !!(props.content && props.content.trim())
)
const heldByAnother = computed(() => !!holder.value && holder.value.id !== props.selfId)
const offersUseCondition = computed(
  () =>
    props.offerConditionReuse &&
    !!holder.value &&
    holder.value.type === props.type &&
    holder.value.id !== props.noReuseOf
)
const renamed = computed(
  () => !!props.originalKey && !!key.value && key.value !== props.originalKey
)

function compose(newSlug: string) {
  return prefix.value && newSlug ? `${prefix.value}-${newSlug}` : ''
}

function onSlugInput() {
  suggestNote.value = ''
  key.value = compose(slug.value)
}

// The parent can set the key itself (a reset, a reloaded artifact): the slug box follows it.
watch(key, (value) => {
  if (value !== compose(slug.value)) slug.value = slugOf(value, props.type)
})

// A new type (the type choice in a create dialog) keeps the slug and changes the prefix: "data-loss" typed as a risk is
// RSK-data-loss, and FR-data-loss once the type is functional requirement.
watch(prefix, () => {
  key.value = compose(slug.value)
})

// Availability, after each pause in typing. Each check carries a number, and an answer to an older check is dropped,
// so a slow reply for "export" never overwrites the answer for "export-csv".
let timer: ReturnType<typeof setTimeout> | undefined
let checkNumber = 0
watch(
  () => [key.value, props.teamId] as const,
  () => {
    holder.value = undefined
    if (timer) clearTimeout(timer)
    if (!props.teamId || !key.value || problem.value) return
    timer = setTimeout(() => void check(), AVAILABILITY_DELAY_MS)
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (timer) clearTimeout(timer)
})

async function check(): Promise<ArtifactKeyHolder | null | undefined> {
  const teamId = props.teamId
  const asked = key.value
  if (!teamId || !asked || slugProblem(slugOf(asked, props.type))) return undefined
  const mine = ++checkNumber
  try {
    const result = await findArtifactKeyHolder(teamId, asked)
    if (mine === checkNumber && asked === key.value) holder.value = result.data
    return result.data
  } catch {
    // Advisory only: the interceptor has shown any error, and the save checks again.
    return undefined
  }
}

// Suggest (UC-ART-create-artifact, "Artifact key rules"): only on a press, and the suggestion replaces the slug box.
async function suggest() {
  const teamId = props.teamId
  if (!teamId || !prefix.value) return
  suggesting.value = true
  suggestNote.value = ''
  try {
    const result = await suggestArtifactKey(teamId, {
      type: props.type,
      title: props.title ?? '',
      content: props.content ?? ''
    })
    if (result.data.artifactKey) {
      key.value = result.data.artifactKey
    } else {
      suggestNote.value = result.data.aiAvailable
        ? 'No suggestion this time. Name it yourself.'
        : 'AI suggestions are temporarily unavailable. Name it yourself.'
    }
  } catch {
    // The interceptor has shown the error; the student can still name the artifact herself.
  } finally {
    suggesting.value = false
  }
}

// For the parent's confirm (step 4, extension 4b): ask the server now, show the answer, and return the holder of the
// current key if it is another artifact.
async function recheck(): Promise<ArtifactKeyHolder | null> {
  if (timer) clearTimeout(timer)
  const found = await check()
  return found && found.id !== props.selfId ? found : null
}

// For a refusal the save got (8c, 10b): show the holder the server named.
function showHolder(found: ArtifactKeyHolder) {
  holder.value = found
}

defineExpose({ recheck, showHolder })
</script>

<style scoped>
.artifact-key-field {
  width: 100%;
}

.key-status {
  min-height: 18px;
  margin-top: 2px;
  font-size: 12px;
  line-height: 18px;
  color: var(--el-text-color-secondary);
  display: flex;
  flex-wrap: wrap;
  gap: 0 8px;
}

.compact .key-status {
  min-height: 0;
}

.key-error {
  color: var(--el-color-danger);
}

.key-free {
  color: var(--el-color-success);
}

.key-warning {
  color: var(--el-color-warning);
}
</style>
