import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import SearchOverlay from '@/components/SearchOverlay.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        'nav.recentSearches': 'Recent Searches',
        'nav.users': 'Users',
        'nav.noResults': 'No Results',
      }
      return map[key] || key
    },
  }),
}))

describe('SearchOverlay', () => {
  it('shows history list when visible and has history items', () => {
    const wrapper = mount(SearchOverlay, {
      props: {
        query: '',
        results: {},
        history: ['用户管理', '角色管理'],
        hasResults: false,
      },
    })
    expect(wrapper.find('.search-history').exists()).toBe(true)
    expect(wrapper.text()).toContain('用户管理')
    expect(wrapper.text()).toContain('角色管理')
  })

  it('shows search results when hasResults=true', () => {
    const wrapper = mount(SearchOverlay, {
      props: {
        query: 'admin',
        results: {
          users: [
            { id: 1, username: 'admin', nickname: '管理员', email: 'admin@test.com' },
          ],
        },
        history: [],
        hasResults: true,
      },
    })
    expect(wrapper.find('.search-results').exists()).toBe(true)
    expect(wrapper.text()).toContain('admin')
  })
})
