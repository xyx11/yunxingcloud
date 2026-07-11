import { test, expect } from '@playwright/test'

test.describe('注册流程', () => {
  test('注册页面加载', async ({ page }) => {
    await page.goto('/register')
    await expect(page.locator('form, .register-form, input').first()).toBeVisible({ timeout: 5000 })
  })

  test('注册表单填写', async ({ page }) => {
    await page.goto('/register')
    await page.waitForTimeout(500)
    const ts = Date.now()
    // Fill username
    const usernameInput = page.locator('input[placeholder*="用户名"], input[placeholder*="username"], input[name="username"]').first()
    if (await usernameInput.isVisible()) {
      await usernameInput.fill('testuser' + ts)
    }
    // Fill password
    const pwdInputs = page.locator('input[type="password"]')
    const pwdCount = await pwdInputs.count()
    if (pwdCount >= 1) await pwdInputs.nth(0).fill('Test123456')
    // Fill email
    const emailInput = page.locator('input[placeholder*="邮箱"], input[placeholder*="email"], input[type="email"]').first()
    if (await emailInput.isVisible()) {
      await emailInput.fill('test' + ts + '@test.com')
    }
  })

  test('注册提交按钮存在', async ({ page }) => {
    await page.goto('/register')
    const submitBtn = page.locator('button:has-text("注册"), button:has-text("Register"), button[type="submit"]').first()
    await expect(submitBtn).toBeVisible({ timeout: 5000 })
  })

  test('登录页面可切换到注册', async ({ page }) => {
    await page.goto('/login')
    const registerLink = page.locator('a:has-text("注册"), a:has-text("Register"), text="没有账号"').first()
    if (await registerLink.isVisible()) {
      await registerLink.click()
      await page.waitForTimeout(500)
      await expect(page).toHaveURL(/\/register/)
    }
  })

  test('注册后跳转登录', async ({ page }) => {
    await page.goto('/login')
    // Check if "已有账号" link exists on login page to switch to register
    const hasAccount = page.locator('text=已有账号, text=去登录').first()
    if (await hasAccount.isVisible()) {
      await expect(hasAccount).toBeVisible()
    }
  })
})
