<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  total: number
  page: number
  size?: number
  maxButtons?: number
}>(), {
  size: 10,
  maxButtons: 5,
})

const emit = defineEmits<{
  'page-change': [page: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

const pages = computed(() => {
  const tp = totalPages.value
  const max = props.maxButtons
  const half = Math.floor(max / 2)
  let start = Math.max(1, props.page - half)
  let end = Math.min(tp, start + max - 1)
  if (end - start + 1 < max) start = Math.max(1, end - max + 1)

  const result: (number | '...')[] = []
  if (start > 1) {
    result.push(1)
    if (start > 2) result.push('...')
  }
  for (let i = start; i <= end; i++) result.push(i)
  if (end < tp) {
    if (end < tp - 1) result.push('...')
    result.push(tp)
  }
  return result
})

function go(p: number) {
  if (p < 1 || p > totalPages.value || p === props.page) return
  emit('page-change', p)
}

const showFirst = computed(() => props.page > 1 && totalPages.value > 1)
const showLast = computed(() => props.page < totalPages.value && totalPages.value > 1)
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination" :aria-label="`Page ${page} of ${totalPages}`">
    <button
      class="pg-btn pg-prev"
      :disabled="!showFirst"
      :aria-label="'Previous page'"
      @click="go(page - 1)"
    >
      &laquo;
    </button>

    <template v-for="p in pages" :key="p">
      <span v-if="p === '...'" class="pg-ellipsis">&hellip;</span>
      <button
        v-else
        class="pg-btn pg-num"
        :class="{ active: p === page }"
        :aria-label="`Page ${p}`"
        :aria-current="p === page ? 'page' : undefined"
        @click="go(p)"
      >
        {{ p }}
      </button>
    </template>

    <button
      class="pg-btn pg-next"
      :disabled="!showLast"
      :aria-label="'Next page'"
      @click="go(page + 1)"
    >
      &raquo;
    </button>

    <span class="pg-info">{{ page }} / {{ totalPages }}</span>
  </nav>
</template>

<style scoped>
.pagination {
  display: flex; align-items: center; justify-content: center; gap: 4px;
  padding: var(--space-lg) 0;
}
.pg-btn {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 36px; height: 36px; padding: 0 8px;
  border: 1px solid var(--border); border-radius: var(--radius-sm);
  background: var(--bg-white); color: var(--text-secondary);
  font-size: var(--font-sm); cursor: pointer;
  transition: all var(--transition-fast);
}
.pg-btn:hover:not(:disabled):not(.active) {
  border-color: var(--jd-red); color: var(--jd-red);
}
.pg-btn.active {
  background: var(--jd-red); color: #fff; border-color: var(--jd-red);
}
.pg-btn:disabled { opacity: .4; cursor: not-allowed; }
.pg-ellipsis { color: var(--text-tertiary); padding: 0 4px; }
.pg-info {
  margin-left: var(--space-sm); font-size: var(--font-xs);
  color: var(--text-tertiary);
}
.pg-prev, .pg-next { font-size: 16px; font-weight: 700; }
@media (max-width: 768px) {
  .pg-info { display: none; }
}
</style>
