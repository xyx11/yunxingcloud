import { ref, onMounted, onUnmounted } from 'vue'

export function usePullRefresh(onRefresh: () => Promise<void>) {
  const pulling = ref(false)
  const refreshing = ref(false)
  const pullDistance = ref(0)
  let startY = 0
  let currentY = 0
  const threshold = 60

  function onTouchStart(e: TouchEvent) {
    if (window.scrollY > 5) return
    startY = e.touches[0].clientY
  }

  function onTouchMove(e: TouchEvent) {
    if (window.scrollY > 5) {
      pulling.value = false
      pullDistance.value = 0
      return
    }
    currentY = e.touches[0].clientY
    const diff = currentY - startY
    if (diff > 0) {
      pulling.value = true
      pullDistance.value = Math.min(diff * 0.5, threshold)
    }
  }

  async function onTouchEnd() {
    if (pullDistance.value >= threshold - 10) {
      refreshing.value = true
      pullDistance.value = threshold
      try {
        await onRefresh()
      } finally {
        refreshing.value = false
        pullDistance.value = 0
        pulling.value = false
      }
    } else {
      pulling.value = false
      pullDistance.value = 0
    }
  }

  onMounted(() => {
    document.addEventListener('touchstart', onTouchStart, { passive: true })
    document.addEventListener('touchmove', onTouchMove, { passive: true })
    document.addEventListener('touchend', onTouchEnd, { passive: true })
  })

  onUnmounted(() => {
    document.removeEventListener('touchstart', onTouchStart)
    document.removeEventListener('touchmove', onTouchMove)
    document.removeEventListener('touchend', onTouchEnd)
  })

  return { pulling, refreshing, pullDistance }
}
