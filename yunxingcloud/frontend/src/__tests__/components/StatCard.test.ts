import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatCard from '@/components/StatCard.vue'

const stubs = {
  NCard: {
    template: '<div class="n-card"><slot/></div>',
  },
  NStatistic: {
    template:
      '<div class="n-statistic"><div class="n-statistic-label">{{ $props.label }}</div><slot name="prefix"/><slot/></div>',
    props: ['label'],
  },
  NSkeleton: {
    template: '<div class="n-skeleton"/>',
  },
}

describe('StatCard', () => {
  it('renders title and value when not loading', () => {
    const wrapper = mount(StatCard, {
      props: { title: '总用户', value: 123, icon: '👥', color: 'blue' },
      global: { stubs },
    })
    expect(wrapper.text()).toContain('总用户')
    expect(wrapper.text()).toContain('123')
  })

  it('shows skeleton when loading=true', () => {
    const wrapper = mount(StatCard, {
      props: { title: '总用户', value: 123, icon: '👥', color: 'blue', loading: true },
      global: { stubs },
    })
    expect(wrapper.find('.n-skeleton').exists()).toBe(true)
    expect(wrapper.text()).not.toContain('123')
  })

  it('applies correct color class based on color prop', () => {
    const wrapper = mount(StatCard, {
      props: { title: 'Revenue', value: '$999', icon: '💰', color: 'green' },
      global: { stubs },
    })
    expect(wrapper.classes()).toContain('stat-green')
  })
})
