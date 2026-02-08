<template>
  <div class="ram-glossary">
    <el-page-header content="Glossary" @back="goBack" />
    <el-card class="ram-glossary__card" shadow="never">
      <h2>Glossary editor coming next</h2>
      <p>This page will provide glossary term search, create, edit, and delete.</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goBack() {
  router.push({ name: 'ram-documents' })
}
</script>

<style scoped>
.ram-glossary {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ram-glossary__card {
  max-width: 720px;
}
</style>
