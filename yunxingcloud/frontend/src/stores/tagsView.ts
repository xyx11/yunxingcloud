import { ref } from 'vue'
import { defineStore } from 'pinia'

export interface TagItem { path: string; name: string; title: string }

function loadTags(): TagItem[] {
  try { const s = sessionStorage.getItem('tagsView'); return s ? JSON.parse(s) : [defaultTag()] } catch { return [defaultTag()] }
}
function saveTags(tags: TagItem[]) { sessionStorage.setItem('tagsView', JSON.stringify(tags)) }
function defaultTag(): TagItem { return { path: '/', name: 'Home', title: '首页' } }

export const useTagsViewStore = defineStore('tagsView', () => {
  const tags = ref<TagItem[]>(loadTags())
  const activePath = ref(tags.value[tags.value.length - 1]?.path || '/')

  function addTag(tag: TagItem) {
    if (!tags.value.find(t => t.path === tag.path)) tags.value.push(tag)
    activePath.value = tag.path
    saveTags(tags.value)
  }

  function removeTag(path: string) {
    const idx = tags.value.findIndex(t => t.path === path)
    tags.value.splice(idx, 1)
    if (activePath.value === path) activePath.value = tags.value[Math.min(idx, tags.value.length - 1)]?.path || '/'
    saveTags(tags.value)
  }

  function closeOther(path: string) {
    const current = tags.value.find(t => t.path === path)
    tags.value = current ? [tags.value[0], current] : [tags.value[0]]
    activePath.value = path
    saveTags(tags.value)
  }

  function closeAll() {
    tags.value = [defaultTag()]
    activePath.value = '/'
    saveTags(tags.value)
  }

  return { tags, activePath, addTag, removeTag, closeOther, closeAll }
})
