export function useKeyboardClick(handler: () => void) {
  function onKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      handler()
    }
  }
  return { onKeydown, role: 'button' as const, tabindex: 0 }
}
