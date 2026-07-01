/**
 * 加购飞入动画 — 从点击位置飞向购物车图标
 * 用法: const { flyToCart } = useCartFly(); flyToCart(event)
 */
export function useCartFly() {
  function flyToCart(e: MouseEvent) {
    const target = document.querySelector('.cart-icon, [aria-label="购物车"], .header-links span:nth-child(3)')
    if (!target) return

    const ball = document.createElement('div')
    ball.style.cssText = 'position:fixed;z-index:9999;width:16px;height:16px;border-radius:50%;background:#e4393c;pointer-events:none;transition:all .6s cubic-bezier(.2,.8,.3,1)'
    ball.style.left = e.clientX - 8 + 'px'
    ball.style.top = e.clientY - 8 + 'px'
    document.body.appendChild(ball)

    requestAnimationFrame(() => {
      const rect = target.getBoundingClientRect()
      ball.style.left = rect.left + rect.width / 2 - 8 + 'px'
      ball.style.top = rect.top + rect.height / 2 - 8 + 'px'
      ball.style.transform = 'scale(.3)'
      ball.style.opacity = '0'
    })

    setTimeout(() => ball.remove(), 700)
  }

  return { flyToCart }
}