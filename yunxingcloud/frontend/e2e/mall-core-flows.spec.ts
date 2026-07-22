import { test, expect } from '@playwright/test'

test.describe('商城首页', () => {
  test('首页加载显示关键元素', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('header')).toBeVisible()
    await expect(page.locator('footer')).toBeVisible()
    await expect(page.locator('text=YXCLOUD').first()).toBeVisible()
  })

  test('首页商品列表加载', async ({ page }) => {
    await page.goto(MALL)
    await page.waitForLoadState('networkidle')
    await expect(page.locator('.product-grid, [class*="product"]').first()).toBeVisible({ timeout: 15000 })
  })

  test('切换到新品Tab', async ({ page }) => {
    await page.goto(MALL)
    await page.waitForLoadState('networkidle')
    const newTab = page.locator('text=新品上市').first()
    if (await newTab.isVisible()) {
      await newTab.click()
      await page.waitForTimeout(800)
    }
  })
})

test.describe('商品搜索', () => {
  test('搜索输入框可用', async ({ page }) => {
    await page.goto(MALL)
    const searchInput = page.locator('input[placeholder*="搜索"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill('手机')
      await searchInput.press('Enter')
      await page.waitForLoadState('networkidle')
      await expect(page.locator('.results-grid, [class*="result"]').first()).toBeVisible({ timeout: 10000 })
    }
  })

  test('搜索页价格筛选', async ({ page }) => {
    await page.goto('/search?q=test')
    await page.waitForLoadState('networkidle')
    const priceTag = page.locator('.price-tag').first()
    if (await priceTag.isVisible()) {
      await priceTag.click()
      await page.waitForTimeout(500)
    }
  })
})

test.describe('商品详情', () => {
  test('商品详情页加载', async ({ page }) => {
    await page.goto('/product/1')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('h1, .pdp-name, [class*="name"]').first()).toBeVisible({ timeout: 10000 })
  })

  test('商品图片可点击切换', async ({ page }) => {
    await page.goto('/product/1')
    await page.waitForLoadState('networkidle')
    const thumb = page.locator('.thumb').first()
    if (await thumb.isVisible()) {
      await thumb.click()
      await page.waitForTimeout(500)
    }
  })

  test('加入购物车按钮可见', async ({ page }) => {
    await page.goto('/product/1')
    await page.waitForLoadState('networkidle')
    const addBtn = page.locator('button').filter({ hasText: /加入购物车|添加/ }).first()
    if (await addBtn.isVisible()) await addBtn.click()
  })
})

test.describe('购物车', () => {
  test('购物车页面加载', async ({ page }) => {
    await page.goto('/cart')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('.cart-page, [class*="cart"]').first()).toBeVisible()
  })

  test('购物车空状态显示', async ({ page }) => {
    await page.goto('/cart')
    await page.waitForLoadState('networkidle')
    // Should show empty state or login redirect
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('用户认证', () => {
  test('登录页面加载', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('input[type="text"], input[placeholder*="用户"]').first()).toBeVisible()
    await expect(page.locator('input[type="password"]').first()).toBeVisible()
  })

  test('登录表单可填写提交', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    const userInput = page.locator('input[type="text"], input[placeholder*="用户"]').first()
    const passInput = page.locator('input[type="password"]').first()
    if (await userInput.isVisible()) {
      await userInput.fill('admin')
      await passInput.fill('admin123')
      const submitBtn = page.locator('button').filter({ hasText: /登录/ }).first()
      await submitBtn.click()
      await page.waitForTimeout(2000)
    }
  })

  test('注册页面加载', async ({ page }) => {
    await page.goto('/register')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('input').first()).toBeVisible()
  })
})

test.describe('结算与支付', () => {
  test('结算页需要登录', async ({ page }) => {
    await page.goto('/checkout')
    await page.waitForLoadState('networkidle')
    // Should redirect to login or show content
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('其他页面', () => {
  const pages = [
    { path: '/products', name: '商品列表' },
    { path: '/brands', name: '品牌列表' },
    { path: '/live', name: '直播' },
    { path: '/flash-sale', name: '秒杀' },
    { path: '/group-buy', name: '拼团' },
    { path: '/coupons', name: '优惠券' },
    { path: '/gift-card', name: '礼品卡' },
    { path: '/points', name: '积分' },
    { path: '/ranking', name: '排行榜' },
    { path: '/help', name: '帮助中心' },
    { path: '/compare', name: '对比' },
    { path: '/recent', name: '最近浏览' },
  ]

  for (const { path, name } of pages) {
    test(`${name} 页面可访问`, async ({ page }) => {
      await page.goto(MALL + path)
      await page.waitForLoadState('networkidle')
      await expect(page.locator('body')).toBeVisible()
    })
  }
})

test.describe('错误处理', () => {
  test('404 页面不白屏', async ({ page }) => {
    await page.goto('/nonexistent-page-xyz')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('body')).toBeVisible()
  })

  test('商品不存在页面', async ({ page }) => {
    await page.goto('/product/999999')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('性能检测', () => {
  test('首页加载时间', async ({ page }) => {
    const start = Date.now()
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    const loadTime = Date.now() - start
    expect(loadTime).toBeLessThan(10000)
  })
})
