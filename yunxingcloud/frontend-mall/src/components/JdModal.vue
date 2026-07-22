<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from '@/locales'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  width?: string
  closable?: boolean
  maskClosable?: boolean
}>(), {
  closable: true,
  maskClosable: true,
  width: '420px',
})

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'close'): void
}>()

const modalRef = ref<HTMLElement>()
const prevFocus = ref<HTMLElement | null>(null)

function close() {
  emit('update:visible', false)
  emit('close')
}

function onMaskClick() {
  if (props.maskClosable) close()
}

function trapFocus(e: KeyboardEvent) {
  if (e.key === 'Escape') { close(); return }
  if (e.key !== 'Tab' || !modalRef.value) return
  const focusable = modalRef.value.querySelectorAll<HTMLElement>(
    'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
  )
  if (!focusable.length) return
  const first = focusable[0]
  const last = focusable[focusable.length - 1]
  if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last.focus() }
  else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first.focus() }
}

watch(() => props.visible, async (v) => {
  if (v) {
    prevFocus.value = document.activeElement as HTMLElement
    document.body.style.overflow = 'hidden'
    await nextTick()
    modalRef.value?.querySelector<HTMLElement>('button:not([disabled]), input:not([disabled])')?.focus()
  } else {
    document.body.style.overflow = ''
    setTimeout(() => prevFocus.value?.focus(), 50)
  }
})

onMounted(() => document.addEventListener('keydown', trapFocus))
onUnmounted(() => document.removeEventListener('keydown', trapFocus))
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click.self="onMaskClick">
      <div ref="modalRef" class="modal-content" :style="{ maxWidth: width }" role="dialog" aria-modal="true" :aria-label="title || ''">
        <div v-if="title || closable" class="modal-header">
          <h3 v-if="title" class="modal-title">{{ title }}</h3>
          <button v-if="closable" class="modal-close" @click="close" :aria-label="t('common.close')">✕</button>
        </div>
        <div class="modal-body">
          <slot />
        </div>
        <div v-if="$slots.footer" class="modal-footer">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: var(--bg-overlay); display: flex; align-items: center; justify-content: center;
  z-index: 999; padding: var(--space-xl);
  animation: fadeIn .2s ease;
}
.modal-content {
  background: var(--bg-white); border-radius: var(--radius-lg); width: 100%;
  box-shadow: var(--shadow-xl); animation: slideUp .25s ease-out;
  max-height: 90vh; display: flex; flex-direction: column;
  outline: none;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-lg) var(--space-xl); border-bottom: 1px solid var(--border-light);
}
.modal-title { font-size: var(--font-lg); font-weight: 700; }
.modal-close {
  background: none; border: none; font-size: var(--font-lg); color: var(--text-tertiary);
  cursor: pointer; padding: var(--space-xs); line-height: 1; border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}
.modal-close:hover { background: var(--bg-hover); color: var(--text-primary); }
.modal-body { padding: var(--space-xl); overflow-y: auto; flex: 1; }
.modal-footer {
  padding: var(--space-lg) var(--space-xl); border-top: 1px solid var(--border-light);
  display: flex; gap: var(--space-md); justify-content: flex-end;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>
