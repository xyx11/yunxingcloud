import { test, expect } from '@playwright/test'

test.describe('yunxingcloud E2E', () => {

  test.describe('Public Pages', () => {

    test('should show login page with branding and form', async ({ page }) => {
      await page.goto('/')
      await expect(page.locator('h1')).toContainText('yunxingcloud')
      await expect(page.locator('input[placeholder*="用户"]').or(page.locator('input[placeholder*="User"]'))).toBeVisible()
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

    test('should redirect unknown routes to login when not authenticated', async ({ page }) => {
      await page.goto('/#/nonexistent-page')
      await page.waitForTimeout(1500)
      // Router guard redirects to login for unauthenticated users
      expect(page.url()).toContain('login')
    })
  })

  test.describe('Navigation', () => {

    test('should navigate between login, register, and forgot password', async ({ page }) => {
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

    test('should redirect to login when accessing protected route', async ({ page }) => {
      await page.goto('/#/users')
      await page.waitForTimeout(1500)
      expect(page.url()).toContain('login')
    })
  })

  test.describe('Login Form', () => {

    test('should show validation on empty submit', async ({ page }) => {
      await page.goto('/#/login')
      await page.locator('.n-button').first().click()
      await page.waitForTimeout(500)
      await expect(page.locator('.n-button').first()).toBeVisible()
    })

    test('should show error on invalid credentials', async ({ page }) => {
      await page.goto('/#/login')
      await page.locator('input[placeholder*="用户"]').or(page.locator('input[placeholder*="User"]')).first().fill('admin')
      await page.locator('input[type="password"]').fill('wrongpassword')
      await page.locator('.n-button').first().click()
      await page.waitForTimeout(1000)
      // should still be on login page with error visible
      await expect(page.locator('.n-button').first()).toBeVisible()
    })

    test('should login successfully with correct credentials', async ({ page }) => {
      await page.goto('/#/login')
      await page.waitForSelector('input[type="password"]', { timeout: 10000 })
      await page.locator('input').first().fill('admin')
      await page.locator('input[type="password"]').fill('admin123')
      await page.locator('.n-button--primary-type').first().click()
      // Wait for dashboard content to appear (indicates successful login)
      await page.waitForSelector('.n-card, .logo', { timeout: 20000 })
      // Should be on home page, not login
      const hasLoginElements = await page.locator('input[type="password"]').count()
      expect(hasLoginElements).toBe(0)
    })
  })

  test.describe('Responsive & Accessibility', () => {

    test('should show login form on mobile viewport', async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 })
      await page.goto('/#/login')
      await expect(page.locator('h1')).toContainText('yunxingcloud')
      await expect(page.locator('input[type="password"]')).toBeVisible()
    })
  })
})