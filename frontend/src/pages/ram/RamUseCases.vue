<template>
  <div class="ram-use-cases">
    <el-page-header content="Use Cases" @back="goBack" />
    <el-card class="ram-use-cases__card" shadow="never">
      <h2>Use case editor coming next</h2>
      <p>This page will provide use case authoring with steps, actors, and conditions.</p>
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
.ram-use-cases {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ram-use-cases__card {
  max-width: 720px;
}
</style>
