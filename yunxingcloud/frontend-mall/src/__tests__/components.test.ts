import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdModal from '@/components/JdModal.vue'
import ProductCard from '@/components/ProductCard.vue'

// Mock IntersectionObserver for jsdom
beforeEach(() => {
  (globalThis as any).IntersectionObserver = class {
    observe() {}
    disconnect() {}
    unobserve() {}
  }
})
afterEach(() => {
  document.body.innerHTML = ''
})

describe('JdButton', () => {
  it('renders default slot', () => {
    const wrapper = mount(JdButton, { slots: { default: '点击' } })
    expect(wrapper.text()).toBe('点击')
  })

  it('has primary class by default', () => {
    const wrapper = mount(JdButton, { slots: { default: 'OK' } })
    expect(wrapper.classes()).toContain('jd-btn--primary')
  })

  it('applies outline class when type=outline', () => {
    const wrapper = mount(JdButton, { props: { type: 'outline' }, slots: { default: 'OK' } })
    expect(wrapper.classes()).toContain('jd-btn--outline')
  })

  it('disables when loading', () => {
    const wrapper = mount(JdButton, { props: { loading: true }, slots: { default: 'OK' } })
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('emits click event', async () => {
    const wrapper = mount(JdButton, { slots: { default: 'OK' } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })

  it('does not emit click when disabled', async () => {
    const wrapper = mount(JdButton, { props: { disabled: true }, slots: { default: 'OK' } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeFalsy()
  })
})

describe('JdBadge', () => {
  it('renders slot content', () => {
    const wrapper = mount(JdBadge, { slots: { default: '热卖' } })
    expect(wrapper.text()).toBe('热卖')
  })

  it('applies type class', () => {
    const wrapper = mount(JdBadge, { props: { type: 'green' }, slots: { default: '新品' } })
    expect(wrapper.classes()).toContain('jd-badge--green')
  })
})

describe('JdEmpty', () => {
  it('renders title', () => {
    const wrapper = mount(JdEmpty, { props: { title: '暂无数据' } })
    expect(wrapper.text()).toContain('暂无数据')
  })

  it('renders description when provided', () => {
    const wrapper = mount(JdEmpty, { props: { description: '请稍后再试' } })
    expect(wrapper.text()).toContain('请稍后再试')
  })

  it('renders action slot', () => {
    const wrapper = mount(JdEmpty, {
      props: { title: '空' },
      slots: { default: '<button>重试</button>' },
    })
    expect(wrapper.html()).toContain('重试')
  })
})

describe('JdModal', () => {
  it('does not render overlay when visible=false', () => {
    mount(JdModal, {
      props: { visible: false },
      slots: { default: 'content' },
    })
    expect(document.querySelector('.modal-overlay')).toBeNull()
  })

  it('renders content to body when visible=true', () => {
    mount(JdModal, {
      props: { visible: true },
      slots: { default: 'modal content text' },
    })
    const overlay = document.querySelector('.modal-overlay')
    expect(overlay).not.toBeNull()
    expect(overlay!.textContent).toContain('modal content text')
  })

  it('renders title in teleported content', () => {
    mount(JdModal, {
      props: { visible: true, title: '确认对话框' },
      slots: { default: 'body' },
    })
    expect(document.querySelector('.modal-overlay')!.textContent).toContain('确认对话框')
  })

  it('emits close when close button clicked', async () => {
    const wrapper = mount(JdModal, {
      props: { visible: true, closable: true },
      slots: { default: 'content' },
    })
    const btn = document.querySelector('.modal-close') as HTMLElement
    btn.click()
    expect(wrapper.emitted('close')).toBeTruthy()
  })
})

describe('ProductCard', () => {
  const product = {
    id: 1,
    name: '测试商品',
    price: 1299,
    originalPrice: 1999,
    sales: 12345,
    rating: 4.5,
    badge: '热卖',
    tags: ['自营', '京东物流'],
  }

  it('renders product name', () => {
    const wrapper = mount(ProductCard, { props: { product } })
    expect(wrapper.text()).toContain('测试商品')
  })

  it('renders price', () => {
    const wrapper = mount(ProductCard, { props: { product } })
    expect(wrapper.text()).toContain('1299')
  })

  it('renders badge', () => {
    const wrapper = mount(ProductCard, { props: { product } })
    expect(wrapper.text()).toContain('热卖')
  })

  it('emits click event with product id', async () => {
    const wrapper = mount(ProductCard, { props: { product } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')?.[0]).toEqual([1])
  })

  it('renders add-cart button by default', () => {
    const wrapper = mount(ProductCard, { props: { product } })
    expect(wrapper.text()).toContain('加入购物车')
  })

  it('hides add-cart button when showAddCart=false', () => {
    const wrapper = mount(ProductCard, { props: { product, showAddCart: false } })
    expect(wrapper.text()).not.toContain('加入购物车')
  })
})
