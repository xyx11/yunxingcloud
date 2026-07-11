<script setup lang="ts">
import { ref } from 'vue'
import { NSpin } from 'naive-ui'

const loading = ref(true)

function onLoad() { loading.value = false }
function onError() { loading.value = false }
</script>

<template>
  <div class="swagger-wrapper">
    <n-spin :show="loading" class="swagger-spin" />
    <iframe
      src="/doc.html"
      class="swagger-iframe"
      @load="onLoad"
      @error="onError"
    />
  </div>
</template>

<style scoped>
.swagger-wrapper { padding: 0; height: calc(100vh - 120px); position: relative; }
.swagger-spin { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
.swagger-iframe { width: 100%; height: 100%; border: none; border-radius: 4px; }
</style>
