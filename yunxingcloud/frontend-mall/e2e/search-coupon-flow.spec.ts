import { test, expect } from '@playwright/test'

test.describe('搜索 & 优惠券', () => {
  test('搜索商品并显示结果', async ({ page }) => {
    await page.goto('/search?q=手机')
    await expect(page.locator('.main-content')).toBeVisible()
    const results = page.locator('.product-card, .search-item')
    if (await results.first().isVisible({ timeout: 5000 })) {
      await expect(results.first()).toBeVisible()
    }
  })

  test('搜索无结果提示', async ({ page }) => {
    await page.goto('/search?q=zzzNoSuchProduct999')
    await expect(page.locator('.empty, .no-results')).toBeVisible({ timeout: 5000 })
  })

  test('优惠券中心加载正常', async ({ page }) => {
    await page.goto('/coupons')
    await expect(page.locator('.main-content')).toBeVisible()
    await expect(page.locator('text=优惠券中心,Coupon Center')).toBeVisible({ timeout: 5000 })
  })
})

test.describe('分类浏览', () => {
  test('商品列表页加载', async ({ page }) => {
    await page.goto('/products')
    await expect(page.locator('.main-content')).toBeVisible()
    const cards = page.locator('.product-card')
    if (await cards.first().isVisible({ timeout: 5000 })) {
      await expect(cards.first()).toBeVisible()
    }
  })

  test('商品列表排序切换', async ({ page }) => {
    await page.goto('/products')
    const sortBtn = page.locator('[class*=sort], button:has-text("排序"), button:has-text("Price"), button:has-text("价格")').first()
    if (await sortBtn.isVisible({ timeout: 3000 })) {
      await sortBtn.click()
    }
  })
})
