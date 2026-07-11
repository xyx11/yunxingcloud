<script setup lang="ts">
import { ref, computed } from 'vue'
import { NDropdown } from 'naive-ui'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  tags: { path: string; name: string; title: string }[]
  activeTag: string
}>()

const emit = defineEmits<{
  'tag-action': [action: string, path: string]
  'click-tag': [path: string]
}>()

const { t } = useI18n()

const ctxMenuTag = ref('')
const ctxMenuX = ref(0)
const ctxMenuY = ref(0)

const contextMenuOptions = computed(() => [
  { label: t('nav.closeCurrent'), key: 'close' },
  { label: t('nav.closeOthers'), key: 'other' },
  { label: t('nav.closeAll'), key: 'all' },
])

const ctxMenuDropdownOptions = computed(() => [
  { label: t('common.refresh'), key: 'refresh' },
  { label: t('common.close'), key: 'close' },
  { label: t('nav.closeLeft'), key: 'closeLeft' },
  { label: t('nav.closeRight'), key: 'closeRight' },
  { label: t('nav.closeOthers'), key: 'other' },
])
</script>

<template>
  <div v-if="tags.length" class="tags-view">
    <span
      v-for="tag in tags" :key="tag.path"
      :class="['tag-item', { active: activeTag === tag.path }]"
      @click="emit('click-tag', tag.path)"
      @contextmenu.prevent="ctxMenuTag = tag.path; ctxMenuX = $event.clientX; ctxMenuY = $event.clientY"
    >
      {{ tag.title }}
      <span v-if="tag.path !== '/'" class="tag-close" @click.stop="emit('tag-action', 'close', tag.path)">×</span>
    </span>
    <n-dropdown trigger="click" :options="contextMenuOptions" @select="(k:string) => emit('tag-action', k, activeTag)">
      <span class="tag-close-all">▼</span>
    </n-dropdown>
    <n-dropdown
      v-if="ctxMenuTag" trigger="manual" :show="!!ctxMenuTag"
      :x="ctxMenuX" :y="ctxMenuY" :options="ctxMenuDropdownOptions"
      @select="(k:string) => { emit('tag-action', k, ctxMenuTag); ctxMenuTag = '' }"
      @clickoutside="ctxMenuTag = ''" placement="bottom-start"
    />
  </div>
</template>

<style scoped>
.tags-view {
  display: flex; align-items: center; gap: 2px; padding: 4px 16px;
  background: var(--n-color, #fff); border-bottom: 1px solid var(--n-border-color, #eee);
  overflow-x: auto; white-space: nowrap;
}
.tag-item {
  display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px;
  font-size: 12px; border-radius: 3px; cursor: pointer; color: #666;
  border: 1px solid transparent; transition: all 0.2s;
}
.tag-item:hover { background: rgba(102,126,234,0.08); }
.tag-item.active { background: rgba(102,126,234,0.12); color: var(--primary); border-color: var(--primary); }
.tag-close { font-size: 14px; line-height: 1; opacity: 0.5; }
.tag-close:hover { opacity: 1; color: #f5576c; }
.tag-close-all { font-size: 10px; cursor: pointer; padding: 4px 6px; opacity: 0.5; }
.tag-close-all:hover { opacity: 1; }
</style>
