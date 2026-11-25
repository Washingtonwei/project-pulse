<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="560px" @open="reset">
    <el-form ref="formRef" :model="draft" :rules="rules" label-width="100px">
      <!-- UC-ART-create-artifact step 2: the caller fixes which types may be created here. One type needs no
           choice; otherwise none is preselected, because an artifact's type can never be changed afterwards. -->
      <el-form-item v-if="types.length > 1" label="Type" prop="type">
        <el-radio-group v-model="draft.type">
          <el-radio v-for="type in types" :key="type" :value="type">{{ typeLabel(type) }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Title" prop="title">
        <el-input v-model="draft.title" maxlength="255" />
      </el-form-item>
      <el-form-item label="Content" prop="content">
        <el-input v-model="draft.content" type="textarea" :autosize="{ minRows: 3 }" />
      </el-form-item>
      <!-- Steps 2 and 3, "Artifact key rules": the student names the key or presses Suggest; extension 4b shows a
           holder here, and Open it uses that artifact instead of creating a new one (4b2). -->
      <el-form-item label="Key" prop="artifactKey">
        <ArtifactKeyField
          ref="keyField"
          v-model="draft.artifactKey"
          :team-id="teamId"
          :type="draft.type"
          :title="draft.title"
          :content="draft.content"
          @open-holder="useHolder"
        />
      </el-form-item>
    </el-form>

    <!-- Step 5 and extension 5a: possible duplicates, advisory only. Use this one picks the existing artifact
         instead of creating another (5a2). -->
    <el-alert
      v-if="duplicateMatches.length"
      type="warning"
      :closable="false"
      show-icon
      title="This team already has similar requirements. Use one of them, or create yours anyway."
    >
      <ul class="duplicate-list">
        <li v-for="match in duplicateMatches" :key="match.id">
          {{ match.artifactKey }}: {{ match.title }}
          <el-button link type="primary" @click="useExisting(match)">Use this one</el-button>
        </li>
      </ul>
    </el-alert>

    <template #footer>
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" :loading="busy" :disabled="!teamId" @click="create">
        {{ duplicateMatches.length ? 'Create anyway' : 'Create' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
// UC-ART-create-artifact from outside a document section: a create-only dialog that a caller opens with the types it
// admits, for example a use case's actor picker with stakeholder and external system (UC-DOC-create-use-case 3a,
// UC-DOC-edit-use-case 6c). The server places the artifact in the home its type gives it (BR-artifact-home) and checks
// the key the student chose in ArtifactKeyField, so the dialog sends the type, key, title and content. It takes no lock
// on that home, as from the artifact
// catalog. The caller learns the result through `created` (a new artifact) or `chosen` (an existing one picked from the
// duplicate warning). Design: docs/design/doc.md, "Creating an actor from the picker".
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import axios from 'axios'
import {
  createRequirementArtifact,
  findRequirementArtifactById,
  searchRequirementArtifacts
} from '@/apis/ram'
import type { ArtifactKeyHolder, RequirementArtifact } from '@/apis/ram/types'
import ArtifactKeyField from '@/components/ArtifactKeyField.vue'
import { slugOf, slugProblem } from '@/utils/artifactKeys'

// The most possible duplicates listed, as in the document editor.
const DUPLICATE_MATCH_LIMIT = 5

const props = withDefaults(
  defineProps<{
    teamId: number | null | undefined
    // The artifact types the caller admits, in the order to offer them.
    types: string[]
    // The title to start from, such as what the student typed into a picker.
    initialTitle?: string
    // Artifacts the duplicate warning must not offer, such as a use case's primary actor in its secondary picker.
    excludeIds?: number[]
  }>(),
  { initialTitle: '', excludeIds: () => [] }
)

const emit = defineEmits<{
  created: [artifact: RequirementArtifact]
  chosen: [artifact: RequirementArtifact]
}>()

const visible = defineModel<boolean>({ required: true })

const formRef = ref<FormInstance>()
const draft = reactive({ type: '', title: '', content: '', artifactKey: '' })
const keyField = ref<InstanceType<typeof ArtifactKeyField>>()
const duplicateMatches = ref<RequirementArtifact[]>([])
// The type and title the duplicate search last ran for: confirming again with both unchanged creates anyway (5a2).
const duplicateCheckedFor = ref<string | null>(null)
const busy = ref(false)

// Extension 4a: each required field is marked where it is, so the error has a location (4a1). The server's 400 is
// the backstop.
const rules: FormRules = {
  type: [{ required: true, message: 'Choose a type', trigger: 'change' }],
  title: [{ required: true, whitespace: true, message: 'Title is required', trigger: 'blur' }],
  content: [{ required: true, whitespace: true, message: 'Content is required', trigger: 'blur' }],
  // The key's format, as the server checks it (4a): a slug of lowercase letters, digits and single hyphens.
  artifactKey: [
    {
      validator: (_rule, _value, callback) => {
        const problem = slugProblem(slugOf(draft.artifactKey, draft.type))
        callback(problem ? new Error(problem) : undefined)
      },
      trigger: 'change'
    }
  ]
}

const dialogTitle = computed(() =>
  props.types.length === 1
    ? `New ${typeLabel(props.types[0] ?? '').toLowerCase()}`
    : 'New requirement'
)

function typeLabel(type: string) {
  // EXTERNAL_SYSTEM becomes "External system".
  const words = type.toLowerCase().replace(/_/g, ' ')
  return words.charAt(0).toUpperCase() + words.slice(1)
}

// Every opening starts clean, from the caller's title.
function reset() {
  draft.type = props.types.length === 1 ? (props.types[0] ?? '') : ''
  draft.title = props.initialTitle.trim()
  draft.content = ''
  draft.artifactKey = ''
  duplicateMatches.value = []
  duplicateCheckedFor.value = null
  formRef.value?.clearValidate()
}

// A warning shown for one type and title no longer applies once either changes: clear it, so the button reads Create
// again and Use this one never offers a match found for the old input.
watch(
  () => [draft.type, draft.title.trim()],
  () => {
    duplicateMatches.value = []
    duplicateCheckedFor.value = null
  }
)

// Step 5: the team's artifacts whose title contains the new one, ignoring case, searched in every type the caller
// admits, not only the chosen one. For actors that catches a stakeholder and an external system with the same name,
// the duplicate that actually happens. Advisory only: if a search fails, it counts as no match.
async function findDuplicates(teamId: number): Promise<RequirementArtifact[]> {
  const title = draft.title.trim()
  const pages = await Promise.all(
    props.types.map((type) =>
      searchRequirementArtifacts(
        teamId,
        { page: 0, size: DUPLICATE_MATCH_LIMIT + props.excludeIds.length },
        { type, title }
      )
        .then((result) => result.data.content)
        .catch(() => [] as RequirementArtifact[])
    )
  )
  return pages
    .flat()
    .filter((match) => match.id == null || !props.excludeIds.includes(match.id))
    .slice(0, DUPLICATE_MATCH_LIMIT)
}

// Steps 4 to 9. The Create button is the confirmation of step 6. A stale duplicate check runs first; when it finds
// matches the dialog shows them and waits. On success the toast names the new key (step 9).
async function create() {
  const teamId = props.teamId
  if (!teamId || !formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  busy.value = true
  try {
    // Step 4, extension 4b: whether another artifact of the team holds the key, asked again now. The field shows the
    // holder, and nothing is created.
    if (await keyField.value?.recheck()) return
    const checkKey = `${draft.type}\n${draft.title.trim()}`
    if (duplicateCheckedFor.value !== checkKey) {
      duplicateMatches.value = await findDuplicates(teamId)
      duplicateCheckedFor.value = checkKey
      if (duplicateMatches.value.length) return
    }
    const result = await createRequirementArtifact(teamId, {
      type: draft.type,
      artifactKey: draft.artifactKey,
      title: draft.title.trim(),
      content: draft.content
    })
    ElMessage.success(`${result.data.artifactKey} created`)
    emit('created', result.data)
    visible.value = false
  } catch (error) {
    // The request interceptor has already shown the server's message (8a, 8b, or a 4a the form missed). Nothing was
    // created, so the dialog stays open with the student's input. A key a teammate took meanwhile (8c) comes back with
    // its holder, which the key field shows.
    const holder = axios.isAxiosError(error) ? error.response?.data?.data : null
    if (holder?.artifactKey) keyField.value?.showHolder(holder as ArtifactKeyHolder)
  } finally {
    busy.value = false
  }
}

// 4b2: the artifact holding the key is used instead of a new one, loaded in full first, as the duplicate warning's
// matches are.
async function useHolder(holder: ArtifactKeyHolder) {
  const teamId = props.teamId
  if (!teamId) return
  try {
    const result = await findRequirementArtifactById(teamId, holder.id)
    useExisting(result.data)
  } catch {
    // The interceptor has shown the error; the dialog stays open.
  }
}

function useExisting(match: RequirementArtifact) {
  emit('chosen', match)
  visible.value = false
}
</script>

<style scoped>
.duplicate-list {
  margin: 4px 0 0;
  padding-left: 18px;
}
</style>
