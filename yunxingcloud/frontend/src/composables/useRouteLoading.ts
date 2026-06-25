import { ref } from 'vue'
import type { Router } from 'vue-router'

const isLoading = ref(false)

export function useRouteLoading(router: Router) {
  router.beforeEach((_to, _from, next) => {
    isLoading.value = true
    next()
  })

  router.afterEach(() => {
    // small delay for visual feedback
    setTimeout(() => { isLoading.value = false }, 300)
  })

  router.onError(() => {
    isLoading.value = false
  })

  return { isLoading }
}