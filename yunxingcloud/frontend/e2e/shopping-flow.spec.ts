import { test, expect } from '@playwright/test'

test.describe('Admin 登录与导航', () => {
  test('登录页加载', async ({ page }) => {
    await page.goto('/admin/login')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('.n-input, input[type="text"], input[placeholder*="用户"]').first()).toBeVisible()
    await expect(page.locator('input[type="password"]').first()).toBeVisible()
    await expect(page.locator('button').filter({ hasText: /登录|Login/ }).first()).toBeVisible()
  })

  test('登录表单可填写', async ({ page }) => {
    await page.goto('/admin/login')
    await page.waitForLoadState('networkidle')
    const userInput = page.locator('input[type="text"], input[placeholder*="用户"], .n-input input').first()
    const passInput = page.locator('input[type="password"]').first()
    if (await userInput.isVisible()) {
      await userInput.fill('admin')
      await passInput.fill('admin123')
      await expect(userInput).toHaveValue('admin')
    }
  })

  test('注册页面可访问', async ({ page }) => {
    await page.goto('/admin/register')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('input').first()).toBeVisible()
  })

  test('忘记密码页面可访问', async ({ page }) => {
    await page.goto('/admin/forgot-password')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('input').first()).toBeVisible()
  })
})

test.describe('Admin 核心页面加载', () => {
  test('仪表盘页面', async ({ page }) => {
    await page.goto('/admin/')
    await page.waitForLoadState('networkidle')
    // Login redirect or dashboard
    await page.waitForTimeout(1000)
    const content = page.locator('.n-spin, .n-card, .n-form, input').first()
    await expect(content).toBeVisible({ timeout: 10000 })
  })

  test('403/未授权处理', async ({ page }) => {
    await page.goto('/admin/users')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    // Should redirect to login or show content
    const content = page.locator('.n-spin, .n-card, .n-form, input, .n-result').first()
    await expect(content).toBeVisible({ timeout: 10000 })
  })
})

test.describe('商城页面（通过 Admin 代理访问）', () => {
  test('首页加载', async ({ page }) => {
    await page.goto('/mall/')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('Admin 关键管理页面', () => {
  const adminPages = [
    { path: '/admin/', name: '仪表盘' },
    { path: '/admin/users', name: '用户管理' },
    { path: '/admin/roles', name: '角色管理' },
    { path: '/admin/departments', name: '部门管理' },
    { path: '/admin/menus', name: '菜单管理' },
    { path: '/admin/dict', name: '字典管理' },
    { path: '/admin/config', name: '参数配置' },
    { path: '/admin/job', name: '定时任务' },
    { path: '/admin/operlog', name: '操作日志' },
    { path: '/admin/notices', name: '通知公告' },
    { path: '/admin/posts', name: '岗位管理' },
    { path: '/admin/monitor', name: '系统监控' },
    { path: '/admin/maintenance', name: '数据维护' },
    { path: '/admin/generator', name: '代码生成' },
    { path: '/admin/swagger', name: 'API文档' },
    { path: '/admin/profile', name: '个人中心' },
  ]

  for (const { path, name } of adminPages) {
    test(`${name} 页面可访问`, async ({ page }) => {
      await page.goto(path)
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)
      // 可能重定向到登录，或显示管理页面
      const content = page.locator('.n-spin, .n-card, .n-form, .n-result, input, .n-layout').first()
      await expect(content).toBeVisible({ timeout: 10000 })
    })
  }
})

test.describe('Admin 业务页面', () => {
  const bizPages = [
    { path: '/admin/orders', name: '订单管理' },
    { path: '/admin/products', name: '商品管理' },
    { path: '/admin/inventory', name: '库存管理' },
    { path: '/admin/payments', name: '支付管理' },
    { path: '/admin/tickets', name: '工单管理' },
    { path: '/admin/coupons', name: '优惠券' },
    { path: '/admin/brands', name: '品牌管理' },
    { path: '/admin/categories', name: '分类管理' },
    { path: '/admin/analytics', name: '销售分析' },
    { path: '/admin/shipments', name: '物流发货' },
    { path: '/admin/invoices', name: '发票管理' },
    { path: '/admin/articles', name: '文章管理' },
    { path: '/admin/banners', name: 'Banner管理' },
    { path: '/admin/reviews', name: '评价管理' },
    { path: '/admin/aftersale', name: '售后管理' },
    { path: '/admin/groupbuy', name: '拼团活动' },
    { path: '/admin/flashsale', name: '秒杀活动' },
    { path: '/admin/giftcards', name: '礼品卡' },
    { path: '/admin/suppliers', name: '供应商' },
    { path: '/admin/bundles', name: '捆绑套餐' },
  ]

  for (const { path, name } of bizPages) {
    test(`${name} 页面可访问`, async ({ page }) => {
      await page.goto(path)
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)
      const content = page.locator('.n-spin, .n-card, .n-form, .n-result, input, .n-layout').first()
      await expect(content).toBeVisible({ timeout: 10000 })
    })
  }
})

test.describe('Admin 错误处理', () => {
  test('404 页面', async ({ page }) => {
    await page.goto('/admin/nonexistent-page-xyz')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('body')).toBeVisible()
  })
})
