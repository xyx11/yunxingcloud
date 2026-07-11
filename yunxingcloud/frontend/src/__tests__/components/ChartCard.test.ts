import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ChartCard from '@/components/ChartCard.vue'

const stubs = {
  NCard: {
    template:
      '<div class="n-card"><div v-if="$props.title" class="n-card-title">{{ $props.title }}</div><slot/></div>',
    props: ['title', 'size'],
  },
  NSkeleton: {
    template: '<div class="n-skeleton"/>',
  },
  NEmpty: {
    template: '<div class="n-empty"/>',
  },
}

describe('ChartCard', () => {
  it('renders title', () => {
    const wrapper = mount(ChartCard, {
      props: { title: '订单趋势' },
      global: { stubs },
    })
    expect(wrapper.text()).toContain('订单趋势')
  })

  it('shows NSkeleton when loading', () => {
    const wrapper = mount(ChartCard, {
      props: { title: 'Chart', loading: true },
      global: { stubs },
    })
    expect(wrapper.find('.n-skeleton').exists()).toBe(true)
  })

  it('shows NEmpty when hasData=false', () => {
    const wrapper = mount(ChartCard, {
      props: { title: 'Chart', hasData: false },
      global: { stubs },
    })
    expect(wrapper.find('.n-empty').exists()).toBe(true)
  })

  it('renders slot content when hasData=true', () => {
    const wrapper = mount(ChartCard, {
      props: { title: 'Chart', hasData: true },
      slots: { default: '<div class="test-chart">图表内容</div>' },
      global: { stubs },
    })
    expect(wrapper.find('.test-chart').exists()).toBe(true)
    expect(wrapper.text()).toContain('图表内容')
  })
})
