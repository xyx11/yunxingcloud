import { onMounted, onUnmounted } from 'vue'

export function useKeyboard(handlers: Record<string, () => void>) {
  function onKeydown(e: KeyboardEvent) {
    const key = [
      e.ctrlKey ? 'Ctrl+' : '',
      e.shiftKey ? 'Shift+' : '',
      e.altKey ? 'Alt+' : '',
      e.key,
    ].join('')

    if (handlers[key]) {
      e.preventDefault()
      handlers[key]()
    }
  }

  onMounted(() => window.addEventListener('keydown', onKeydown))
  onUnmounted(() => window.removeEventListener('keydown', onKeydown))
}
