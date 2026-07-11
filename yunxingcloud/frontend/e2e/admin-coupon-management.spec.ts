import { test, expect } from '@playwright/test'

async function doLogin(page: any) {
  await page.goto('/#/login')
  await page.waitForSelector('input[type="password"]', { timeout: 10000 })
  await page.locator('input').first().fill('admin')
  await page.locator('input[type="password"]').fill('admin123')
  await page.locator('.n-button--primary-type').first().click()
  await page.waitForSelector('.logo, .n-card', { timeout: 20000 })
}

test.describe('Coupon Management', () => {
  test('coupon list loads', async ({ page }) => {
    await doLogin(page)
    await page.goto('/#/coupons')
    await page.waitForSelector('.n-card, .n-data-table', { timeout: 10000 })
    await expect(page.locator('.n-card, .n-data-table').first()).toBeVisible()
  })

  test('create new coupon', async ({ page }) => {
    await doLogin(page)
    await page.goto('/#/coupons')
    await page.waitForSelector('.n-button', { timeout: 10000 })
    await page.locator('button:has-text("新增"), button:has-text("添加")').first().click()
    await page.waitForTimeout(500)
    // Fill coupon form
    const inputs = page.locator('.n-input input')
    const count = await inputs.count()
    if (count > 0) await inputs.first().fill('TestCoupon' + Date.now())
    await page.locator('button:has-text("确定"), button:has-text("保存")').first().click()
    await page.waitForTimeout(1000)
  })
})
