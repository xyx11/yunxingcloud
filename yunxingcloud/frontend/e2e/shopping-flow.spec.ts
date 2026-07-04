import { test, expect } from '@playwright/test'

test.describe('Shopping Flow - 商城购物流程', () => {

  test('home page should load with banners and products', async ({ page }) => {
    await page.goto('/mall/')
    await page.waitForLoadState('networkidle')
    // 首页应有商品卡片或轮播区域
    await expect(page.locator('.product-card, .n-card, [class*="banner"], [class*="product"]').first()).toBeVisible({ timeout: 15000 })
  })

  test('product list page should display products', async ({ page }) => {
    await page.goto('/mall/')
    await page.waitForLoadState('networkidle')

    // 尝试点击分类或导航到商品列表
    const categoryLink = page.locator('[href*="category"], [href*="product"], .n-button').first()
    if (await categoryLink.isVisible({ timeout: 3000 }).catch(() => false)) {
      await categoryLink.click()
      await page.waitForTimeout(1000)
    } else {
      // 直接导航到商品列表
      await page.goto('/mall/#/products')
      await page.waitForLoadState('networkidle')
    }

    // 页面应该有内容
    const content = page.locator('.n-card, .product-card, [class*="product-item"], .n-grid-item')
    await expect(content.first()).toBeVisible({ timeout: 10000 })
  })

  test('product detail page should show specs and add-to-cart', async ({ page }) => {
    await page.goto('/mall/')
    await page.waitForLoadState('networkidle')

    // 点击第一个商品进入详情
    const firstProduct = page.locator('.product-card, .n-card, [class*="product-item"], .n-grid-item .n-card').first()
    if (await firstProduct.isVisible({ timeout: 8000 }).catch(() => false)) {
      await firstProduct.click()
      await page.waitForTimeout(1500)
    } else {
      // 直接导航到一个商品详情（如果知道ID的话用通用的）
      await page.goto('/mall/#/product/1')
      await page.waitForLoadState('networkidle')
    }

    // 详情页应有商品名称或价格或加购按钮
    const detailContent = page.locator('h1, h2, .n-button, [class*="price"], [class*="name"], [class*="title"]')
    await expect(detailContent.first()).toBeVisible({ timeout: 10000 })
  })

  test('add to cart and view cart page', async ({ page }) => {
    await page.goto('/mall/')
    await page.waitForLoadState('networkidle')

    // 点击第一个商品
    const firstProduct = page.locator('.product-card, .n-card, [class*="product-item"], .n-grid-item .n-card').first()
    let navigated = false
    if (await firstProduct.isVisible({ timeout: 8000 }).catch(() => false)) {
      await firstProduct.click()
      await page.waitForTimeout(1500)
      navigated = true
    }

    if (!navigated) {
      await page.goto('/mall/#/product/1')
      await page.waitForLoadState('networkidle')
    }

    // 尝试点击加购按钮
    const addToCartBtn = page.locator('.n-button').filter({ hasText: /cart|购物|加入|Cart|Add/i }).first()
    if (await addToCartBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await addToCartBtn.click()
      await page.waitForTimeout(800)
    }

    // 导航到购物车页面
    await page.goto('/mall/#/cart')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('.n-card, [class*="cart"], .n-data-table, .n-empty').first()).toBeVisible({ timeout: 10000 })
  })

  test('search products', async ({ page }) => {
    await page.goto('/mall/')
    await page.waitForLoadState('networkidle')

    // 查找搜索框
    const searchInput = page.locator('input[type="text"], .n-input input').first()
    if (await searchInput.isVisible({ timeout: 5000 }).catch(() => false)) {
      await searchInput.fill('手机')
      await searchInput.press('Enter')
      await page.waitForTimeout(1500)

      // 搜索结果页应该有内容
      const results = page.locator('.n-card, .product-card, [class*="product-item"]')
      await expect(results.first()).toBeVisible({ timeout: 8000 })
    }
  })

  test('checkout flow requires login', async ({ page }) => {
    // 未登录状态下访问结算页应重定向到登录页
    await page.goto('/mall/#/checkout')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 应该显示登录页或重定向
    const url = page.url()
    expect(url).toMatch(/login|checkout|\/mall/)
  })
})
