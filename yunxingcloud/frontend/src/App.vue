<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'
import { useRouteLoading } from '@/composables/useRouteLoading'
import { useThemeProvider } from '@/composables/useThemeProvider'
import { useRouter } from 'vue-router'
import { NConfigProvider, NMessageProvider, NButton, NResult } from 'naive-ui'
import { zhCN, dateZhCN } from 'naive-ui'

const router = useRouter()
const { isLoading } = useRouteLoading(router)
const { currentTheme } = useThemeProvider()

const hasError = ref(false)
const errorMessage = ref('')

onErrorCaptured((err, _instance, info) => {
  console.error(`[ErrorBoundary] ${info}:`, err)
  const msg = err instanceof Error ? err.message : String(err)
  const stack = err instanceof Error ? (err.stack || '').substring(0, 500) : ''
  errorMessage.value = msg + (stack ? '\n' + stack : '')
  hasError.value = true
  return false
})
</script>

<template>
  <n-config-provider :theme="currentTheme" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <div class="route-loading-bar" :class="{ active: isLoading }" />
      <template v-if="hasError">
        <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:var(--bg,#f0f2f5)">
          <n-result status="500" title="Application Error" :description="errorMessage">
            <template #footer>
              <n-button type="primary" @click="hasError = false; router.push('/')">Back to Home</n-button>
              <n-button @click="hasError = false; router.go(0)">Reload Page</n-button>
            </template>
          </n-result>
        </div>
      </template>
      <router-view v-else />
    </n-message-provider>
  </n-config-provider>
</template>

<style>
.route-loading-bar {
  position: fixed; top: 0; left: 0; right: 0; height: 3px; z-index: 10000;
  background: linear-gradient(90deg, #667eea, #43e97b);
  transform: scaleX(0); transform-origin: left;
  transition: transform 0.3s ease;
  opacity: 0;
  pointer-events: none;
}
.route-loading-bar.active {
  opacity: 1;
  transform: scaleX(0.7);
  transition: transform 10s cubic-bezier(0, 0, 0.2, 1);
}
</style>