<template>
  <div class="ram-editor">
    <el-page-header content="Document Editor" @back="goBack" />
    <el-card class="ram-editor__card" shadow="never">
      <h2>Editor coming next</h2>
      <p>
        This is the placeholder editor for document ID {{ documentId }}. We will replace this with
        the section-based authoring UI in the next step.
      </p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const documentId = computed(() => route.params.documentId)

function goBack() {
  router.push({ name: 'ram-documents' })
}
</script>

<style scoped>
.ram-editor {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ram-editor__card {
  max-width: 720px;
}
</style>
