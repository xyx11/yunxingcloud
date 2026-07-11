import { ref, computed, watch, type Ref } from 'vue'
import type { DataTableColumn } from 'naive-ui'

export function useColumnManager<T>(
  allColumnsRef: Ref<DataTableColumn<T>[]>,
  tableId: string,
) {
  const storageKey = `table_cols_hidden.${tableId}`

  function loadHidden(): Set<string> {
    try {
      const raw = localStorage.getItem(storageKey)
      return raw ? new Set(JSON.parse(raw)) : new Set()
    } catch {
      return new Set()
    }
  }

  const hiddenKeys = ref<Set<string>>(loadHidden())

  watch(hiddenKeys, (val) => {
    localStorage.setItem(storageKey, JSON.stringify([...val]))
  }, { deep: true })

  const visibleColumns = computed(() =>
    allColumnsRef.value.filter((col: DataTableColumn<T>) => {
      if (!col.key) return true
      return !hiddenKeys.value.has(col.key)
    }),
  )

  function toggleColumn(key: string) {
    const next = new Set(hiddenKeys.value)
    if (next.has(key)) next.delete(key)
    else next.add(key)
    hiddenKeys.value = next
  }

  function resetColumns() {
    hiddenKeys.value = new Set()
  }

  return { visibleColumns, hiddenKeys, toggleColumn, resetColumns }
}
