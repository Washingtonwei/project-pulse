<template>
  <div class="ram-editor">
    <el-page-header content="Document Editor" @back="goBack" />

    <el-alert
      v-if="!hasTeam"
      type="warning"
      show-icon
      title="No team assigned"
      description="Ask your instructor to assign you to a team before authoring requirements."
    />

    <el-skeleton v-else-if="loading" :rows="6" animated />

    <div v-else class="ram-editor__layout">
      <el-card class="ram-editor__sidebar" shadow="never">
        <template #header>
          <div class="sidebar-header">
            <div>
              <div class="doc-title">{{ document?.title }}</div>
              <div class="doc-meta">{{ document?.type }}</div>
            </div>
            <el-button type="primary" plain size="small" @click="refresh">Refresh</el-button>
          </div>
        </template>

        <el-menu
          class="section-menu"
          :default-active="String(selectedSectionId)"
          @select="handleSelect"
        >
          <el-menu-item
            v-for="section in document?.sections || []"
            :key="section.id"
            :index="String(section.id)"
          >
            <span class="section-title">{{ section.title }}</span>
            <el-tag
              v-if="section.lock?.locked"
              size="small"
              type="warning"
              class="lock-tag"
            >
              Locked
            </el-tag>
          </el-menu-item>
        </el-menu>
      </el-card>

      <el-card class="ram-editor__content" shadow="never">
        <template #header>
          <div class="content-header">
            <div>
              <h2>{{ selectedSection?.title || 'Select a section' }}</h2>
              <div class="section-meta" v-if="selectedSection">
                <el-tag type="info" size="small">{{ selectedSection.type }}</el-tag>
                <span class="section-key">{{ selectedSection.sectionKey }}</span>
              </div>
            </div>
            <div class="content-actions" v-if="selectedSection">
              <el-button
                v-if="canLock"
                type="primary"
                plain
                :loading="locking"
                @click="lockSection"
              >
                Lock
              </el-button>
              <el-button
                v-if="canUnlock"
                type="warning"
                plain
                :loading="unlocking"
                @click="unlockSection"
              >
                Unlock
              </el-button>
              <el-button
                type="success"
                :disabled="!canEdit"
                :loading="saving"
                @click="saveSection"
              >
                Save
              </el-button>
            </div>
          </div>
        </template>

        <el-empty v-if="!selectedSection" description="Select a section to start editing." />

        <div v-else class="content-body">
          <div class="sticky-info">
            <el-alert v-if="lockInfo?.locked" type="warning" show-icon class="lock-alert">
              <template #title>
                Section locked by {{ lockInfo.lockedBy?.name || 'another user' }}
              </template>
              <template #default>
                <div>Expires at: {{ formatTimestamp(lockInfo.expiresAt) }}</div>
                <div v-if="lockInfo.reason">Reason: {{ lockInfo.reason }}</div>
              </template>
            </el-alert>

            <el-collapse v-if="selectedSection.guidance" class="guidance" :model-value="['guidance']">
              <el-collapse-item name="guidance" title="Guidance">
                <el-alert type="info" show-icon :closable="false">
                  <template #default>
                    <pre>{{ selectedSection.guidance }}</pre>
                  </template>
                </el-alert>
              </el-collapse-item>
            </el-collapse>
          </div>

          <div v-if="selectedSection.type === 'RICH_TEXT'" class="rich-text-editor">
            <div class="toolbar">
              <el-button-group>
                <el-button
                  size="small"
                  :type="editor?.isActive('heading', { level: 1 }) ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
                >
                  H1
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('heading', { level: 2 }) ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
                >
                  H2
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('heading', { level: 3 }) ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
                >
                  H3
                </el-button>
              </el-button-group>

              <el-button-group>
                <el-button
                  size="small"
                  :type="editor?.isActive('bold') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleBold().run()"
                >
                  Bold
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('italic') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleItalic().run()"
                >
                  Italic
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('underline') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleUnderline().run()"
                >
                  Underline
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('strike') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleStrike().run()"
                >
                  Strike
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('code') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleCode().run()"
                >
                  Code
                </el-button>
              </el-button-group>

              <el-button-group>
                <el-button
                  size="small"
                  :type="editor?.isActive('bulletList') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleBulletList().run()"
                >
                  Bullets
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('orderedList') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleOrderedList().run()"
                >
                  Numbered
                </el-button>
                <el-button
                  size="small"
                  :type="editor?.isActive('blockquote') ? 'primary' : 'default'"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().toggleBlockquote().run()"
                >
                  Quote
                </el-button>
                <el-button
                  size="small"
                  :disabled="!canEdit"
                  @click="editor?.chain().focus().setHorizontalRule().run()"
                >
                  Rule
                </el-button>
              </el-button-group>

              <el-button-group>
                <el-button size="small" :disabled="!canEdit" @click="openLinkDialog">
                  Link
                </el-button>
                <el-button size="small" :disabled="!canEdit" @click="unsetLink">
                  Unlink
                </el-button>
              </el-button-group>

              <el-button-group>
                <el-button
                  size="small"
                  :disabled="!editor?.can().undo()"
                  @click="editor?.chain().focus().undo().run()"
                >
                  Undo
                </el-button>
                <el-button
                  size="small"
                  :disabled="!editor?.can().redo()"
                  @click="editor?.chain().focus().redo().run()"
                >
                  Redo
                </el-button>
              </el-button-group>
            </div>
            <EditorContent :editor="editor" class="editor-content" />
          </div>

          <div v-else class="list-editor">
            <div class="list-header">
              <h3>Requirements</h3>
              <el-button type="primary" :disabled="!canEdit" @click="openAddArtifact">
                Add Requirement
              </el-button>
            </div>
            <el-table :data="draftArtifacts" border stripe>
              <el-table-column label="Key" prop="artifactKey" width="110" />
              <el-table-column label="Type" prop="type" width="180" />
              <el-table-column label="Title" prop="title" />
              <el-table-column label="Priority" prop="priority" width="120" />
              <el-table-column label="Actions" width="140">
                <template #default="{ $index }">
                  <el-button size="small" :disabled="!canEdit" @click="openEditArtifact($index)">
                    Edit
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    :disabled="!canEdit"
                    @click="removeArtifact($index)"
                  >
                    Delete
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="artifactDialogVisible" title="Requirement" width="600px">
      <el-form label-width="140px">
        <el-form-item label="Type">
          <el-select v-model="artifactDraft.type" filterable placeholder="Select type">
            <el-option v-for="type in artifactTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="Title">
          <el-input v-model="artifactDraft.title" />
        </el-form-item>
        <el-form-item label="Content">
          <el-input v-model="artifactDraft.content" type="textarea" :autosize="{ minRows: 4 }" />
        </el-form-item>
        <el-form-item label="Priority">
          <el-select v-model="artifactDraft.priority" placeholder="Select priority">
            <el-option v-for="priority in priorities" :key="priority" :label="priority" :value="priority" />
          </el-select>
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="artifactDraft.notes" type="textarea" :autosize="{ minRows: 2 }" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="artifactDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveArtifact">Save</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="linkDialogVisible" title="Insert Link" width="420px">
      <el-form label-width="90px">
        <el-form-item label="URL">
          <el-input v-model="linkUrl" placeholder="https://example.com" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="linkDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="applyLink">Apply</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EditorContent, useEditor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'
import Link from '@tiptap/extension-link'
import { useUserInfoStore } from '@/stores/userInfo'
import type { Student } from '@/apis/student/types'
import type {
  RequirementArtifactSummary,
  RequirementDocument,
  RequirementDocumentSection,
  RequirementDocumentSectionLock
} from '@/apis/ram/types'
import {
  findDocumentById,
  getDocumentSectionLock,
  lockDocumentSection,
  unlockDocumentSection,
  updateDocumentSection
} from '@/apis/ram'

const router = useRouter()
const route = useRoute()
const userInfoStore = useUserInfoStore()

const documentId = computed(() => Number(route.params.documentId))
const teamId = computed(() => (userInfoStore.userInfo as Student | null)?.teamId ?? null)
const currentUserId = computed(() => (userInfoStore.userInfo as Student | null)?.id ?? null)
const hasTeam = computed(() => Boolean(teamId.value))

const document = ref<RequirementDocument | null>(null)
const selectedSectionId = ref<number | null>(null)

const loading = ref(false)
const saving = ref(false)
const locking = ref(false)
const unlocking = ref(false)

const draftContent = ref('')
const draftArtifacts = ref<RequirementArtifactSummary[]>([])

const artifactDialogVisible = ref(false)
const artifactDraft = ref<RequirementArtifactSummary>({
  type: 'OTHER',
  title: '',
  content: '',
  priority: 'MEDIUM',
  notes: ''
})
const artifactEditingIndex = ref<number | null>(null)
const linkDialogVisible = ref(false)
const linkUrl = ref('')

const artifactTypes = [
  'GLOSSARY_TERM',
  'BUSINESS_PROBLEM',
  'BUSINESS_OPPORTUNITY',
  'BUSINESS_OBJECTIVE',
  'SUCCESS_METRIC',
  'VISION_STATEMENT',
  'BUSINESS_RISK',
  'ASSUMPTION',
  'RISK',
  'STAKEHOLDER',
  'BUSINESS_RULE',
  'FUNCTIONAL_REQUIREMENT',
  'FEATURE',
  'USE_CASE',
  'PRECONDITION',
  'POSTCONDITION',
  'USER_STORY',
  'QUALITY_ATTRIBUTE',
  'EXTERNAL_INTERFACE_REQUIREMENT',
  'CONSTRAINT',
  'DATA_REQUIREMENT',
  'OTHER'
]

const priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

function formatTimestamp(value?: string | null) {
  if (!value) return 'unknown'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}

const editor = useEditor({
  extensions: [
    StarterKit,
    Underline,
    Link.configure({
      openOnClick: false,
      HTMLAttributes: {
        rel: 'noopener noreferrer',
        target: '_blank'
      }
    })
  ],
  content: draftContent.value,
  editable: false,
  onUpdate({ editor }) {
    draftContent.value = editor.getHTML()
  }
})

const selectedSection = computed<RequirementDocumentSection | null>(() => {
  if (!document.value || selectedSectionId.value === null) return null
  return document.value.sections.find((section) => section.id === selectedSectionId.value) || null
})

const lockInfo = computed<RequirementDocumentSectionLock | null>(() =>
  selectedSection.value?.lock || null
)

const lockedByMe = computed(() => {
  if (!lockInfo.value?.locked) return false
  if (!currentUserId.value) return false
  return lockInfo.value.lockedBy?.id === currentUserId.value
})

const canLock = computed(() => Boolean(selectedSection.value && !lockInfo.value?.locked))
const canUnlock = computed(() => Boolean(selectedSection.value && lockInfo.value?.locked && lockedByMe.value))
const canEdit = computed(() => Boolean(selectedSection.value && lockedByMe.value))

function goBack() {
  router.push({ name: 'ram-documents' })
}

async function loadDocument() {
  if (!teamId.value || !documentId.value) return
  loading.value = true
  try {
    const result = await findDocumentById(teamId.value, documentId.value)
    document.value = result.data
    if (document.value.sections.length) {
      selectedSectionId.value = document.value.sections[0].id
    }
  } finally {
    loading.value = false
  }
}

function handleSelect(id: string) {
  selectedSectionId.value = Number(id)
}

function refresh() {
  loadDocument()
}

watch(selectedSection, (section) => {
  if (!section) return
  draftContent.value = section.content || ''
  if (section.type === 'RICH_TEXT' && editor.value) {
    editor.value.commands.setContent(draftContent.value, false)
  }
  draftArtifacts.value = (section.requirementArtifacts || []).map((artifact) => ({ ...artifact }))
})

watch(canEdit, (value) => {
  if (editor.value) editor.value.setEditable(value)
})

async function lockSection() {
  if (!teamId.value || !documentId.value || !selectedSection.value) return
  locking.value = true
  try {
    const result = await lockDocumentSection(teamId.value, documentId.value, selectedSection.value.id, {})
    selectedSection.value.lock = result.data
    ElMessage.success('Section locked')
  } finally {
    locking.value = false
  }
}

async function unlockSection() {
  if (!teamId.value || !documentId.value || !selectedSection.value) return
  unlocking.value = true
  try {
    await unlockDocumentSection(teamId.value, documentId.value, selectedSection.value.id)
    const refreshed = await getDocumentSectionLock(teamId.value, documentId.value, selectedSection.value.id)
    selectedSection.value.lock = refreshed.data
    ElMessage.success('Section unlocked')
  } finally {
    unlocking.value = false
  }
}

async function saveSection() {
  if (!teamId.value || !documentId.value || !selectedSection.value) return
  saving.value = true
  try {
    const payload = {
      id: selectedSection.value.id,
      sectionKey: selectedSection.value.sectionKey,
      type: selectedSection.value.type,
      title: selectedSection.value.title,
      content: draftContent.value,
      requirementArtifacts:
        selectedSection.value.type === 'LIST'
          ? draftArtifacts.value.map((artifact) => ({
              ...artifact,
              sourceSectionId: selectedSection.value!.id
            }))
          : [],
      guidance: selectedSection.value.guidance
    }

    const result = await updateDocumentSection(
      teamId.value,
      documentId.value,
      selectedSection.value.id,
      payload
    )

    const updated = result.data
    if (document.value) {
      const index = document.value.sections.findIndex((section) => section.id === updated.id)
      if (index >= 0) document.value.sections[index] = updated
    }
    ElMessage.success('Section saved')
  } finally {
    saving.value = false
  }
}

function openAddArtifact() {
  artifactEditingIndex.value = null
  artifactDraft.value = {
    type: 'OTHER',
    title: '',
    content: '',
    priority: 'MEDIUM',
    notes: ''
  }
  artifactDialogVisible.value = true
}

function openEditArtifact(index: number) {
  artifactEditingIndex.value = index
  artifactDraft.value = { ...draftArtifacts.value[index] }
  artifactDialogVisible.value = true
}

function removeArtifact(index: number) {
  draftArtifacts.value.splice(index, 1)
}

function saveArtifact() {
  if (!artifactDraft.value.title || !artifactDraft.value.content) {
    ElMessage.warning('Title and content are required.')
    return
  }

  if (artifactEditingIndex.value === null) {
    draftArtifacts.value.push({ ...artifactDraft.value })
  } else {
    draftArtifacts.value[artifactEditingIndex.value] = { ...artifactDraft.value }
  }

  artifactDialogVisible.value = false
}

function openLinkDialog() {
  if (!editor.value) return
  linkUrl.value = editor.value.getAttributes('link').href || ''
  linkDialogVisible.value = true
}

function applyLink() {
  if (!editor.value) return
  const url = linkUrl.value.trim()
  if (!url) {
    editor.value.chain().focus().unsetLink().run()
    linkDialogVisible.value = false
    return
  }
  editor.value.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
  linkDialogVisible.value = false
}

function unsetLink() {
  editor.value?.chain().focus().unsetLink().run()
}

async function ensureDocumentLoaded() {
  await loadDocument()
}

onMounted(ensureDocumentLoaded)

watch(
  () => teamId.value,
  async (value, previous) => {
    if (!value || value === previous) return
    await ensureDocumentLoaded()
  }
)

watch(
  () => documentId.value,
  async (value, previous) => {
    if (!value || value === previous) return
    await ensureDocumentLoaded()
  }
)

function handleKeydown(event: KeyboardEvent) {
  if (!selectedSection.value || !canEdit.value) return
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
    event.preventDefault()
    saveSection()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.ram-editor {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ram-editor__layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  align-items: stretch;
}

.ram-editor__sidebar {
  min-height: 400px;
  height: calc(100vh - 170px);
  overflow: auto;
}

.ram-editor__content {
  height: calc(100vh - 170px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.ram-editor__content :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.doc-title {
  font-weight: 600;
}

.doc-meta {
  font-size: 12px;
  color: #6b7280;
}

.section-menu {
  border-right: none;
}

.section-title {
  display: inline-block;
  max-width: 170px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lock-tag {
  margin-left: 6px;
}

.ram-editor__content h2 {
  margin: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.section-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

.section-key {
  font-family: 'Fira Mono', monospace;
}

.content-actions {
  display: flex;
  gap: 8px;
}

.content-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.rich-text-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-height: 0;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.editor-content {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 12px;
  min-height: 220px;
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: #fff;
}

.editor-content :deep(.ProseMirror) {
  min-height: 196px;
  outline: none;
}

.editor-content :deep(.ProseMirror p) {
  margin: 0 0 0.75rem 0;
}

.editor-content :deep(.ProseMirror p:last-child) {
  margin-bottom: 0;
}

.guidance pre {
  white-space: pre-wrap;
  font-family: inherit;
  margin: 0;
}

.lock-alert {
  margin-bottom: 8px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

@media (max-width: 960px) {
  .ram-editor__layout {
    grid-template-columns: 1fr;
  }
}
</style>
