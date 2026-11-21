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
      <el-button type="primary" :loading="busy" @click="create">
        {{ duplicateMatches.length ? 'Create anyway' : 'Create' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
// UC-ART-create-artifact from outside a document section: a create-only dialog that a caller opens with the types it
// admits, for example a use case's actor picker with stakeholder and external system (UC-DOC-create-use-case 3a,
// UC-DOC-edit-use-case 6c). The server places the artifact in the home its type gives it (BR-artifact-home) and mints
// its key, so the dialog sends only the type, title and content. It takes no lock on that home, as from the artifact
// catalog. The caller learns the result through `created` (a new artifact) or `chosen` (an existing one picked from the
// duplicate warning). Design: docs/design/doc.md, "Creating an actor from the picker".
import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createRequirementArtifact, searchRequirementArtifacts } from '@/apis/ram'
import type { RequirementArtifact } from '@/apis/ram/types'

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
const draft = reactive({ type: '', title: '', content: '' })
const duplicateMatches = ref<RequirementArtifact[]>([])
// The type and title the duplicate search last ran for: confirming again with both unchanged creates anyway (5a2).
const duplicateCheckedFor = ref<string | null>(null)
const busy = ref(false)

// Extension 4a: each required field is marked where it is, so the error has a location (4a1). The server's 400 is
// the backstop.
const rules: FormRules = {
  type: [{ required: true, message: 'Choose a type', trigger: 'change' }],
  title: [{ required: true, whitespace: true, message: 'Title is required', trigger: 'blur' }],
  content: [{ required: true, whitespace: true, message: 'Content is required', trigger: 'blur' }]
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
  duplicateMatches.value = []
  duplicateCheckedFor.value = null
  formRef.value?.clearValidate()
}

// Step 5: the team's artifacts of the chosen type whose title contains the new one, ignoring case. Advisory only: if
// the search fails, the create goes ahead as if nothing matched.
async function findDuplicates(teamId: number): Promise<RequirementArtifact[]> {
  try {
    const result = await searchRequirementArtifacts(
      teamId,
      { page: 0, size: DUPLICATE_MATCH_LIMIT + props.excludeIds.length },
      { type: draft.type, title: draft.title.trim() }
    )
    return result.data.content
      .filter((match) => match.id == null || !props.excludeIds.includes(match.id))
      .slice(0, DUPLICATE_MATCH_LIMIT)
  } catch {
    return []
  }
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
    const checkKey = `${draft.type}\n${draft.title.trim()}`
    if (duplicateCheckedFor.value !== checkKey) {
      duplicateMatches.value = await findDuplicates(teamId)
      duplicateCheckedFor.value = checkKey
      if (duplicateMatches.value.length) return
    }
    const result = await createRequirementArtifact(teamId, {
      type: draft.type,
      title: draft.title.trim(),
      content: draft.content
    })
    ElMessage.success(`${result.data.artifactKey} created`)
    emit('created', result.data)
    visible.value = false
  } catch {
    // The request interceptor has already shown the server's message (8a, 8b, or a 4a the form missed). Nothing was
    // created, so the dialog stays open with the student's input.
  } finally {
    busy.value = false
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
