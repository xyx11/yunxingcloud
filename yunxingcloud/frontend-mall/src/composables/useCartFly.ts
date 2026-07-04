export function useCartFly() {
  function flyToCart(e: MouseEvent, count = 1) {
    const target = document.querySelector('.cart-icon, [aria-label="购物车"], .header-links span')
    if (!target) return

    // Flying dot
    const dot = document.createElement('div')
    dot.style.cssText = `
      position:fixed;z-index:9999;width:18px;height:18px;border-radius:50%;
      background:#f10215;pointer-events:none;
      transition:all .55s cubic-bezier(.2,.9,.25,1);
      box-shadow:0 2px 8px rgba(241,2,21,.4);
    `
    dot.style.left = e.clientX - 9 + 'px'
    dot.style.top = e.clientY - 9 + 'px'
    document.body.appendChild(dot)

    // "+1" badge
    const badge = document.createElement('div')
    badge.style.cssText = `
      position:fixed;z-index:10000;pointer-events:none;
      font-size:13px;font-weight:700;color:#f10215;
      transition:all .8s cubic-bezier(.2,.8,.3,1);
      text-shadow:0 1px 2px rgba(0,0,0,.1);
    `
    badge.style.left = e.clientX + 10 + 'px'
    badge.style.top = e.clientY - 15 + 'px'
    badge.textContent = '+' + count
    document.body.appendChild(badge)

    // Animate badge up and fade
    requestAnimationFrame(() => {
      badge.style.transform = 'translateY(-32px)'
      badge.style.opacity = '0'
    })

    // Animate dot to cart
    requestAnimationFrame(() => {
      const rect = target.getBoundingClientRect()
      dot.style.left = rect.left + rect.width / 2 - 9 + 'px'
      dot.style.top = rect.top + rect.height / 2 - 9 + 'px'
      dot.style.transform = 'scale(.2)'
      dot.style.opacity = '0'
    })

    setTimeout(() => { dot.remove(); badge.remove() }, 900)
  }

  return { flyToCart }
}