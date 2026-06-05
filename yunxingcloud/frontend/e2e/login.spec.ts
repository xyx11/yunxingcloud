import { test, expect } from '@playwright/test'

test.describe('yunxingcloud E2E', () => {

  test('should show login page', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('h1')).toContainText('yunxingcloud')
    await expect(page.locator('input[placeholder*="用户名"]')).toBeVisible()
    await expect(page.locator('input[type="password"]')).toBeVisible()
  })

  test('should show login page on direct access', async ({ page }) => {
    await page.goto('/#/login')
    await expect(page.locator('h1')).toContainText('yunxingcloud')
  })

  test('should show register page', async ({ page }) => {
    await page.goto('/#/register')
    await expect(page.locator('h1')).toContainText('创建账号')
  })

  test('should redirect to login when accessing protected route', async ({ page }) => {
    await page.goto('/#/users')
    await page.waitForTimeout(1000)
    // should be redirected to login page
    const url = page.url()
    expect(url).toContain('login')
  })
})
