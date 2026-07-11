import { test, expect } from '@playwright/test'

async function doLogin(page: any) {
  await page.goto('/#/login')
  await page.waitForSelector('input[type="password"]', { timeout: 10000 })
  await page.locator('input').first().fill('admin')
  await page.locator('input[type="password"]').fill('admin123')
  await page.locator('.n-button--primary-type').first().click()
  await page.waitForSelector('.logo, .n-card', { timeout: 20000 })
}

test.describe('User CRUD', () => {
  test('create new user', async ({ page }) => {
    await doLogin(page)
    await page.goto('/#/users')
    await page.waitForSelector('.n-button', { timeout: 10000 })
    // Click add button (look for button with "+" or "新增" text)
    await page.locator('button:has-text("新增"), button:has-text("添加")').first().click()
    await page.waitForTimeout(500)
    // Fill form - username, password, email
    // Use unique username with timestamp to avoid conflicts
    const ts = Date.now()
    await page.locator('input').nth(0).fill('testuser' + ts)
    if (await page.locator('input[type="password"]').isVisible()) {
      await page.locator('input[type="password"]').fill('test123456')
    }
    // Submit
    await page.locator('button:has-text("确定"), button:has-text("保存")').first().click()
    await page.waitForTimeout(1000)
  })

  test('search users', async ({ page }) => {
    await doLogin(page)
    await page.goto('/#/users')
    await page.waitForSelector('.n-card, .n-data-table', { timeout: 10000 })
    // Find search input if exists
    const searchInput = page.locator('.n-input input, input[placeholder*="搜索"], input[placeholder*="search"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill('admin')
      await searchInput.press('Enter')
      await page.waitForTimeout(500)
    }
  })

  test('view user list loads', async ({ page }) => {
    await doLogin(page)
    await page.goto('/#/users')
    await page.waitForSelector('.n-card, .n-data-table, .n-spin', { timeout: 10000 })
    await expect(page.locator('.n-card, .n-data-table, .n-layout').first()).toBeVisible()
  })
})
