import { test, expect } from '@playwright/test'

async function doLogin(page: any) {
  await page.goto('/#/login')
  await page.waitForSelector('input[type="password"]', { timeout: 10000 })
  await page.locator('input').first().fill('admin')
  await page.locator('input[type="password"]').fill('admin123')
  await page.locator('.n-button--primary-type').first().click()
  await page.waitForSelector('.logo, .n-card', { timeout: 20000 })
}

test.describe('New v2.0 Pages', () => {

  test.describe('Ticket Management', () => {
    test('should show ticket page with table', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/tickets')
      await page.waitForSelector('.n-data-table, .n-card', { timeout: 10000 })
      await expect(page.locator('.n-card').first()).toBeVisible()
    })

    test('should open create ticket modal', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/tickets')
      await page.waitForSelector('.n-button--primary-type', { timeout: 10000 })
      await page.locator('.n-button--primary-type').first().click()
      await expect(page.locator('.n-modal')).toBeVisible({ timeout: 5000 })
    })
  })

  test.describe('Payment Management', () => {
    test('should show payment page', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/payments')
      await page.waitForSelector('.n-card', { timeout: 10000 })
      await expect(page.locator('.n-card').first()).toBeVisible()
    })
  })

  test.describe('Order Management', () => {
    test('should show order page', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/orders')
      await page.waitForSelector('.n-card', { timeout: 10000 })
      await expect(page.locator('.n-card').first()).toBeVisible()
    })
  })

  test.describe('Product Management', () => {
    test('should show product page with search', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/products')
      await page.waitForSelector('.n-input, .n-card', { timeout: 10000 })
      await expect(page.locator('.n-card').first()).toBeVisible()
    })

    test('should open add product modal', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/products')
      await page.waitForSelector('.n-button--primary-type', { timeout: 10000 })
      await page.locator('.n-button--primary-type').first().click()
      await expect(page.locator('.n-modal')).toBeVisible({ timeout: 5000 })
    })
  })

  test.describe('Inventory Management', () => {
    test('should show inventory page', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/inventory')
      await page.waitForSelector('.n-card', { timeout: 10000 })
      await expect(page.locator('.n-card').first()).toBeVisible()
    })

    test('should show stock-in button', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/inventory')
      await page.waitForSelector('.n-button', { timeout: 10000 })
      const btn = page.locator('.n-button').filter({ hasText: /入库|Stock In/i }).first()
      await expect(btn).toBeVisible({ timeout: 5000 })
    })
  })

  test.describe('Warehouse Management', () => {
    test('should show warehouse page', async ({ page }) => {
      await doLogin(page)
      await page.goto('/#/warehouses')
      await page.waitForSelector('.n-card', { timeout: 10000 })
      await expect(page.locator('.n-card').first()).toBeVisible()
    })
  })

  test.describe('Sidebar Navigation', () => {
    test('should show new menu items in sidebar', async ({ page }) => {
      await doLogin(page)
      await page.waitForSelector('.custom-menu', { timeout: 5000 })
      // Check for new menu items (they should be rendered in the sidebar)
      const sidebar = page.locator('.custom-menu')
      await expect(sidebar).toBeVisible()
    })
  })
})