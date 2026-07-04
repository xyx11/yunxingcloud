<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'primary' | 'outline' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  block?: boolean
}>(), {
  type: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false,
})

const emit = defineEmits<{ (e: 'click'): void }>()
</script>

<template>
  <button
    class="jd-btn"
    :class="[
      `jd-btn--${type}`,
      `jd-btn--${size}`,
      { 'jd-btn--block': block, 'jd-btn--loading': loading },
    ]"
    :disabled="disabled || loading"
    @click="emit('click')"
  >
    <span v-if="loading" class="btn-spinner" />
    <slot />
  </button>
</template>

<style scoped>
.jd-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
  font-family: inherit;
}

.jd-btn:disabled { opacity: .6; cursor: not-allowed; }

/* Sizes */
.jd-btn--sm { height: 28px; padding: 0 var(--space-md); font-size: var(--font-xs); border-radius: var(--radius-sm); }
.jd-btn--md { height: 36px; padding: 0 var(--space-lg); font-size: var(--font-md); }
.jd-btn--lg { height: 44px; padding: 0 var(--space-xxl); font-size: var(--font-lg); border-radius: var(--radius-lg); }

/* Types */
.jd-btn--primary { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.jd-btn--primary:hover:not(:disabled) { background: var(--jd-red-dark); border-color: var(--jd-red-dark); }

.jd-btn--outline { background: var(--bg-white); color: var(--jd-red); border-color: var(--jd-red); }
.jd-btn--outline:hover:not(:disabled) { background: var(--jd-red-light); }

.jd-btn--ghost { background: transparent; color: var(--text-primary); border-color: var(--border); }
.jd-btn--ghost:hover:not(:disabled) { border-color: var(--jd-red); color: var(--jd-red); }

.jd-btn--danger { background: var(--red-status); color: #fff; border-color: var(--red-status); }
.jd-btn--danger:hover:not(:disabled) { opacity: .85; }

.jd-btn--block { width: 100%; }

.btn-spinner {
  width: 14px; height: 14px; border: 2px solid currentColor; border-top-color: transparent;
  border-radius: 50%; animation: spin .6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
