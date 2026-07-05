<script setup lang="ts">
import { watch, onUnmounted } from 'vue'
import JdButton from '@/components/JdButton.vue'

const props = withDefaults(defineProps<{
  show: boolean
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
}>(), {
  title: '提示',
  confirmText: '确定',
  cancelText: '取消',
})

const emit = defineEmits(['confirm', 'cancel'])

function onKeydown(e: KeyboardEvent) { if (e.key === 'Escape') emit('cancel') }
watch(() => props.show, (v) => {
  if (v) document.addEventListener('keydown', onKeydown)
  else document.removeEventListener('keydown', onKeydown)
})
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="cd-overlay" @click.self="emit('cancel')">
      <div class="cd-dialog" role="dialog" aria-modal="true">
        <h3 class="cd-title">{{ title }}</h3>
        <p class="cd-message">{{ message }}</p>
        <div class="cd-actions">
          <JdButton type="outline" @click="emit('cancel')">{{ cancelText }}</JdButton>
          <JdButton @click="emit('confirm')">{{ confirmText }}</JdButton>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.cd-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: var(--bg-overlay); display: flex; align-items: center;
  justify-content: center; z-index: 1000; animation: fadeIn .2s;
}
.cd-dialog {
  background: var(--bg-white); border-radius: var(--radius-xl); padding: 32px;
  max-width: 400px; width: 90%; box-shadow: var(--shadow-lg);
  animation: slideUp .25s ease-out;
}
.cd-title { font-size: 18px; font-weight: 700; margin-bottom: 12px; }
.cd-message { color: var(--text-secondary); font-size: var(--font-md); margin-bottom: 24px; line-height: 1.5; }
.cd-actions { display: flex; gap: 12px; justify-content: flex-end; }

@keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0 } to { transform: translateY(0); opacity: 1 } }
</style>
