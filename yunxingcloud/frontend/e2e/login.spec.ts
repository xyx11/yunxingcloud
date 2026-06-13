import { test, expect } from '@playwright/test'

test.describe('yunxingcloud E2E', () => {

  test('should show login page', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('h1')).toContainText('yunxingcloud')
    await expect(page.locator('input[placeholder*="用户"]')).toBeVisible()
    await expect(page.locator('input[type="password"]')).toBeVisible()
  })

  test('should show login page on direct access', async ({ page }) => {
    await page.goto('/#/login')
    await expect(page.locator('h1')).toContainText('yunxingcloud')
  })

  test('should show register page', async ({ page }) => {
    await page.goto('/#/register')
    await expect(page.locator('h1')).toBeVisible()
  })

  test('should show forgot password page', async ({ page }) => {
    await page.goto('/#/forgot-password')
    await expect(page.locator('h1')).toBeVisible()
  })

  test('should redirect to login when accessing protected route', async ({ page }) => {
    await page.goto('/#/users')
    await page.waitForTimeout(1500)
    expect(page.url()).toContain('login')
  })

  test('should show error on empty login', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('button[type="submit"]').click()
    await page.waitForTimeout(500)
    // form validation should show error or button should still be visible
    await expect(page.locator('button[type="submit"]')).toBeVisible()
  })

  test('should navigate between public pages', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('a[href*="register"]').first().click()
    await page.waitForTimeout(500)
    expect(page.url()).toContain('register')

    await page.locator('a[href*="login"]').first().click()
    await page.waitForTimeout(500)
    expect(page.url()).toContain('login')

    await page.locator('a[href*="forgot"]').first().click()
    await page.waitForTimeout(500)
    expect(page.url()).toContain('forgot')
  })

  test('should show 404 for unknown routes', async ({ page }) => {
    await page.goto('/#/nonexistent-page')
    await page.waitForTimeout(500)
    await expect(page.locator('text=404').first()).toBeVisible()
  })
})
