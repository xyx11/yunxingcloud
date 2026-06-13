import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTagsViewStore } from '@/stores/tagsView'

describe('tagsView Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
  })

  it('initializes with home tag', () => {
    const store = useTagsViewStore()
    expect(store.tags.length).toBeGreaterThanOrEqual(1)
    expect(store.tags.find(t => t.path === '/')).toBeTruthy()
  })

  it('addTag adds new tag', () => {
    const store = useTagsViewStore()
    store.addTag({ path: '/users', name: 'Users', title: '用户管理' })
    expect(store.tags.find(t => t.path === '/users')).toBeTruthy()
  })

  it('removeTag removes tag', () => {
    const store = useTagsViewStore()
    store.addTag({ path: '/roles', name: 'Roles', title: '角色管理' })
    const initialCount = store.tags.length
    store.removeTag('/roles')
    expect(store.tags.length).toBe(initialCount - 1)
  })

  it('closeAll keeps home', () => {
    const store = useTagsViewStore()
    store.addTag({ path: '/users', name: 'Users', title: '用户管理' })
    store.closeAll()
    expect(store.tags.length).toBe(1)
    expect(store.tags[0].path).toBe('/')
  })
})
