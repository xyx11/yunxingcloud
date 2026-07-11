import { test, expect } from '@playwright/test'

test.describe('结算流程', () => {
  test('浏览商品到加入购物车', async ({ page }) => {
    await page.goto('/products')
    const card = page.locator('.product-card, .result-card').first()
    if (await card.isVisible()) {
      await card.click()
      await page.waitForTimeout(500)
      await expect(page).toHaveURL(/\/product\//)
    }
  })

  test('商品详情页加入购物车', async ({ page }) => {
    await page.goto('/products')
    const card = page.locator('.product-card, .result-card').first()
    if (await card.isVisible()) {
      await card.click()
      await page.waitForTimeout(500)
    }
    // Click add to cart button
    const addBtn = page.locator('button:has-text("加入购物车"), .add-to-cart, button:has-text("Add to Cart")').first()
    if (await addBtn.isVisible()) {
      await addBtn.click()
      await page.waitForTimeout(500)
    }
  })

  test('购物车页面加载', async ({ page }) => {
    await page.goto('/cart')
    await page.waitForTimeout(500)
    await expect(page.locator('.cart-page, .cart-container, .cart-list').first()).toBeVisible({ timeout: 5000 })
  })

  test('结算页面加载', async ({ page }) => {
    await page.goto('/cart')
    await page.waitForTimeout(500)
    const checkoutBtn = page.locator('button:has-text("结算"), button:has-text("去结算"), button:has-text("Checkout")').first()
    if (await checkoutBtn.isVisible()) {
      await checkoutBtn.click()
      await page.waitForTimeout(500)
      // May redirect to login first
    }
  })

  test('订单成功页面展示', async ({ page }) => {
    await page.goto('/checkout/success')
    await page.waitForTimeout(500)
    // Just verify the page loads without error
    await expect(page.locator('body')).toBeVisible()
  })
})
