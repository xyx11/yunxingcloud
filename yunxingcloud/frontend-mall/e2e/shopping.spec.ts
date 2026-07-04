import { test, expect } from '@playwright/test'

test.describe('核心购物流程', () => {
  test('首页加载正常', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('.main-content')).toBeVisible()
    await expect(page.locator('h1.logo')).toContainText('YXCLOUD')
  })

  test('商品列表浏览', async ({ page }) => {
    await page.goto('/products')
    await expect(page.locator('.product-grid, .plp')).toBeVisible()
  })

  test('搜索功能', async ({ page }) => {
    await page.goto('/search?q=手机')
    await expect(page.locator('.results-title')).toBeVisible()
  })

  test('从首页到商品详情', async ({ page }) => {
    await page.goto('/')
    const productCard = page.locator('.product-card-home, .product-card').first()
    if (await productCard.isVisible()) {
      await productCard.click()
      await expect(page).toHaveURL(/\/product\/\d+/)
    }
  })
})
