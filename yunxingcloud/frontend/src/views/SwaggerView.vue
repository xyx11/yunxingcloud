<script setup lang="ts">
import { ref } from 'vue'
import { NSpin } from 'naive-ui'

const loading = ref(true)

function onLoad() { loading.value = false }
function onError() { loading.value = false }
</script>

<template>
  <div style="padding:0; height:calc(100vh - 120px); position:relative">
    <n-spin :show="loading" style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center" />
    <iframe
      src="/doc.html"
      style="width:100%; height:100%; border:none; border-radius:4px;"
      @load="onLoad"
      @error="onError"
    />
  </div>
</template>
