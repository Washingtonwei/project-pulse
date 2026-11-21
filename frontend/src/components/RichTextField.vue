<template>
  <div class="rich-text-field" :class="{ 'is-readonly': !editable }">
    <!-- The toolbar shows only while the field can be edited; a read-only field is just its text. -->
    <div v-if="editable" class="toolbar">
      <el-button-group>
        <el-button size="small" :type="isActive('bold') ? 'primary' : 'default'" @click="run((c) => c.toggleBold())">
          Bold
        </el-button>
        <el-button size="small" :type="isActive('italic') ? 'primary' : 'default'" @click="run((c) => c.toggleItalic())">
          Italic
        </el-button>
      </el-button-group>
      <el-button-group>
        <el-button
          size="small"
          :type="isActive('bulletList') ? 'primary' : 'default'"
          @click="run((c) => c.toggleBulletList())"
        >
          Bullet list
        </el-button>
        <el-button
          size="small"
          :type="isActive('orderedList') ? 'primary' : 'default'"
          @click="run((c) => c.toggleOrderedList())"
        >
          Numbered list
        </el-button>
      </el-button-group>
      <el-button-group>
        <el-button size="small" @click="openLinkDialog">Link</el-button>
        <el-button size="small" @click="run((c) => c.unsetLink())">Unlink</el-button>
      </el-button-group>
    </div>
    <EditorContent :editor="editor" class="content" />

    <el-dialog v-model="linkDialogVisible" title="Link" width="420px" append-to-body>
      <el-input v-model="linkUrl" placeholder="https://..." @keyup.enter="applyLink" />
      <div v-if="linkError" class="link-error">{{ linkError }}</div>
      <template #footer>
        <el-button @click="linkDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="applyLink">Apply</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// A small rich text field for the use case template's Associated Information, Assumptions and Open Issues
// (docs/design/doc.md, "The template's text fields"). v-model is the HTML Tiptap produces; the server stores it as
// sent. Stored HTML is only ever shown through Tiptap, never with v-html: Tiptap parses it into its own schema first,
// so markup outside that schema is dropped rather than rendered.
import { onBeforeUnmount, ref, watch } from 'vue'
import { EditorContent, useEditor } from '@tiptap/vue-3'
import type { ChainedCommands } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'

const props = withDefaults(
  defineProps<{
    modelValue?: string | null
    editable?: boolean
  }>(),
  { modelValue: '', editable: false }
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// Only these link targets can be stored through the editor, so a javascript: URL never becomes a link.
const ALLOWED_LINK = /^(https?:\/\/|mailto:)/i

// What an empty Tiptap document serializes to. An untouched field emits '' instead, so it stays empty.
const EMPTY_DOCUMENT = '<p></p>'

const editor = useEditor({
  extensions: [
    // StarterKit 3 bundles Link and Underline; Link is configured here rather than registered a second time.
    StarterKit.configure({
      link: {
        // Never follow a link while editing. A read-only field is not contenteditable, so the browser follows
        // its links itself, in a new tab.
        openOnClick: false,
        isAllowedUri: (url, ctx) => ALLOWED_LINK.test(url) && ctx.defaultValidate(url),
        HTMLAttributes: { rel: 'noopener noreferrer', target: '_blank' }
      }
    })
  ],
  content: props.modelValue || '',
  editable: props.editable,
  onUpdate({ editor }) {
    const html = editor.getHTML()
    emit('update:modelValue', html === EMPTY_DOCUMENT ? '' : html)
  }
})

// A new value from outside (another use case selected, a reload after a save) replaces the content without emitting an
// update, and only when it differs from what the editor already holds, so the cursor does not jump while typing.
watch(
  () => props.modelValue,
  (value) => {
    const instance = editor.value
    if (!instance) return
    const next = value || ''
    const current = instance.getHTML()
    if (next === current || (next === '' && current === EMPTY_DOCUMENT)) return
    instance.commands.setContent(next, { emitUpdate: false })
  }
)

// Taking or releasing the use case lock turns editing on or off.
watch(
  () => props.editable,
  (value) => editor.value?.setEditable(value, false)
)

onBeforeUnmount(() => editor.value?.destroy())

function isActive(name: string) {
  return Boolean(editor.value?.isActive(name))
}

function run(command: (chain: ChainedCommands) => ChainedCommands) {
  if (!editor.value) return
  command(editor.value.chain().focus()).run()
}

const linkDialogVisible = ref(false)
const linkUrl = ref('')
const linkError = ref('')

function openLinkDialog() {
  if (!editor.value) return
  linkUrl.value = editor.value.getAttributes('link').href || ''
  linkError.value = ''
  linkDialogVisible.value = true
}

// An empty URL removes the link; anything but http, https or mailto is refused at the dialog.
function applyLink() {
  const url = linkUrl.value.trim()
  if (!url) {
    run((c) => c.extendMarkRange('link').unsetLink())
    linkDialogVisible.value = false
    return
  }
  if (!ALLOWED_LINK.test(url)) {
    linkError.value = 'A link must start with http://, https:// or mailto:'
    return
  }
  run((c) => c.extendMarkRange('link').setLink({ href: url }))
  linkDialogVisible.value = false
}
</script>

<style scoped>
.rich-text-field {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.rich-text-field.is-readonly {
  border-color: var(--el-border-color-lighter);
  background: var(--el-fill-color-lighter);
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.content :deep(.ProseMirror) {
  min-height: 3em;
  padding: 6px 10px;
  outline: none;
}

.content :deep(.ProseMirror p) {
  margin: 0 0 0.4em;
}

.content :deep(.ProseMirror ul),
.content :deep(.ProseMirror ol) {
  padding-left: 1.4em;
  margin: 0 0 0.4em;
}

.link-error {
  margin-top: 6px;
  color: var(--el-color-danger);
  font-size: 12px;
}
</style>
