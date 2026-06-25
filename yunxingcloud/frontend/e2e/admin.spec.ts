import { test, expect } from '@playwright/test'

async function doLogin(page: any) {
  await page.goto('/#/login')
  await page.waitForSelector('input[type="password"]', { timeout: 10000 })
  await page.locator('input').first().fill('admin')
  await page.locator('input[type="password"]').fill('admin123')
  await page.locator('.n-button--primary-type').first().click()
  await page.waitForSelector('.logo, .n-card', { timeout: 20000 })
}

test.describe('Admin Dashboard', () => {

  test('should login successfully', async ({ page }) => {
    await doLogin(page)
    await expect(page.locator('.logo').first()).toBeVisible({ timeout: 10000 })
  })

  test('should show sidebar navigation', async ({ page }) => {
    await doLogin(page)
    await expect(page.locator('.custom-menu').first()).toBeVisible({ timeout: 5000 })
  })

  test('should show user area with username', async ({ page }) => {
    await doLogin(page)
    await expect(page.locator('.user-area').first()).toBeVisible({ timeout: 5000 })
  })
})