import { ref, onMounted, onUnmounted } from 'vue'

interface InfiniteScrollOptions {
  threshold?: number
  onLoad: () => Promise<void>
}

export function useInfiniteScroll(options: InfiniteScrollOptions) {
  const loadingMore = ref(false)
  const hasMore = ref(true)
  const { threshold = 300, onLoad } = options

  async function loadMore() {
    if (loadingMore.value || !hasMore.value) return
    loadingMore.value = true
    try {
      await onLoad()
    } finally {
      loadingMore.value = false
    }
  }

  function onScroll() {
    if (loadingMore.value || !hasMore.value) return
    const h = document.documentElement
    if (h.scrollTop + h.clientHeight >= h.scrollHeight - threshold) {
      loadMore()
    }
  }

  function reset() {
    hasMore.value = true
  }

  onMounted(() => {
    window.addEventListener('scroll', onScroll)
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', onScroll)
  })

  return { loadingMore, hasMore, loadMore, reset }
}