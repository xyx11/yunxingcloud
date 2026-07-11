import { test, expect } from '@playwright/test'

async function doLogin(page: any) {
  await page.goto('/#/login')
  await page.waitForSelector('input[type="password"]', { timeout: 10000 })
  await page.locator('input').first().fill('admin')
  await page.locator('input[type="password"]').fill('admin123')
  await page.locator('.n-button--primary-type').first().click()
  await page.waitForSelector('.logo, .n-card', { timeout: 20000 })
}

test.describe('Order Lifecycle', () => {
  test('order list loads', async ({ page }) => {
    await doLogin(page)
    // Navigate to orders (path may be /#/orders or /#/order)
    await page.goto('/#/orders')
    await page.waitForSelector('.n-card, .n-data-table, .n-spin', { timeout: 10000 })
    await expect(page.locator('.n-card, .n-data-table').first()).toBeVisible()
  })

  test('view order details', async ({ page }) => {
    await doLogin(page)
    await page.goto('/#/orders')
    await page.waitForSelector('.n-data-table, .n-card', { timeout: 10000 })
    // Click first order if exists
    const firstRow = page.locator('.n-data-table tbody tr').first()
    if (await firstRow.isVisible()) {
      await firstRow.click()
      await page.waitForTimeout(500)
    }
  })
})
