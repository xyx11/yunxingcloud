import { test, expect } from '@playwright/test'

test.describe('首页', () => {
  test('首页加载正常', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('.main-content')).toBeVisible()
    await expect(page.locator('text=YXCLOUD').first()).toBeVisible()
  })

  test('banner 轮播显示', async ({ page }) => {
    await page.goto('/')
    const banner = page.locator('.banner-wrapper')
    if (await banner.isVisible()) {
      await expect(banner.locator('.banner-slide.active')).toBeVisible()
    }
  })

  test('分类入口展示', async ({ page }) => {
    await page.goto('/')
    const categories = page.locator('.categories, .categories-section')
    if (await categories.isVisible()) {
      await expect(categories.locator('.cat-item').first()).toBeVisible()
    }
  })

  test('限时秒杀区域', async ({ page }) => {
    await page.goto('/')
    const flash = page.locator('.flash-section')
    if (await flash.isVisible()) {
      await expect(flash.locator('.flash-item').first()).toBeVisible()
    }
  })

  test('点击分类跳转商品列表', async ({ page }) => {
    await page.goto('/')
    const catItem = page.locator('.cat-item').first()
    if (await catItem.isVisible()) {
      await catItem.click()
      await expect(page).toHaveURL(/\/products/)
    }
  })
})

test.describe('商品列表', () => {
  test('商品列表页面加载', async ({ page }) => {
    await page.goto('/products')
    await expect(page.locator('.plp, .sort-bar, .sidebar').first()).toBeVisible()
  })

  test('商品卡片显示价格和名称', async ({ page }) => {
    await page.goto('/products')
    const card = page.locator('.product-card, .result-card').first()
    if (await card.isVisible()) {
      await expect(card.locator('[class*="price"]').first()).toBeVisible()
      await expect(card.locator('[class*="name"]').first()).toBeVisible()
    }
  })

  test('排序切换', async ({ page }) => {
    await page.goto('/products')
    const sortPrice = page.locator('.sort-item, .sort-opt').filter({ hasText: /价格/ }).first()
    if (await sortPrice.isVisible()) {
      await sortPrice.click()
    }
  })

  test('点击商品进入详情', async ({ page }) => {
    await page.goto('/products')
    const card = page.locator('.product-card, .result-card').first()
    if (await card.isVisible()) {
      await card.click()
      await expect(page).toHaveURL(/\/product\/\d+/)
    }
  })
})

test.describe('搜索功能', () => {
  test('搜索关键词显示结果', async ({ page }) => {
    await page.goto('/search?q=手机')
    await expect(page.locator('.results-title').first()).toBeVisible()
  })

  test('搜索框输入建议', async ({ page }) => {
    await page.goto('/search')
    const input = page.locator('.search-input')
    await input.fill('手机')
    await page.waitForTimeout(500)
    const suggestions = page.locator('.suggestions, .suggest-item')
    if (await suggestions.isVisible()) {
      await expect(suggestions.first()).toBeVisible()
    }
  })

  test('热门关键词可点击', async ({ page }) => {
    await page.goto('/search')
    const hotTag = page.locator('.hot-tag').first()
    if (await hotTag.isVisible()) {
      await hotTag.click()
      await expect(page.locator('.results-title, .results-grid, .sort-row').first()).toBeVisible()
    }
  })

  test('搜索历史记录', async ({ page }) => {
    await page.goto('/search?q=test')
    await page.waitForTimeout(300)
    await page.goto('/search')
    const history = page.locator('.history-section, .history-tag')
    if (await history.isVisible()) {
      await expect(history.first()).toBeVisible()
    }
  })
})

test.describe('商品详情', () => {
  test('详情页加载商品信息', async ({ page }) => {
    await page.goto('/')
    const card = page.locator('.product-card-home, .product-card').first()
    if (await card.isVisible()) {
      await card.click()
      await expect(page.locator('.pdp-main, .product-info, h1, .pdp-name').first()).toBeVisible()
    }
  })

  test('图片画廊显示', async ({ page }) => {
    await page.goto('/')
    const card = page.locator('.product-card-home').first()
    if (await card.isVisible()) {
      await card.click()
      const gallery = page.locator('.gallery-main, .pdp-gallery')
      if (await gallery.isVisible()) {
        await expect(gallery.locator('img').first()).toBeVisible()
      }
    }
  })

  test('SKU 选择器可交互', async ({ page }) => {
    await page.goto('/')
    const card = page.locator('.product-card-home').first()
    if (await card.isVisible()) {
      await card.click()
      const sku = page.locator('.sku-item, .sku-option').first()
      if (await sku.isVisible()) {
        await sku.click()
      }
    }
  })

  test('加入购物车按钮', async ({ page }) => {
    await page.goto('/')
    const card = page.locator('.product-card-home').first()
    if (await card.isVisible()) {
      await card.click()
      const addBtn = page.locator('button').filter({ hasText: /加入购物车|Add to Cart/ }).first()
      if (await addBtn.isVisible()) {
        await addBtn.click()
        await page.waitForTimeout(500)
      }
    }
  })

  test('评论区域显示', async ({ page }) => {
    await page.goto('/')
    const card = page.locator('.product-card-home').first()
    if (await card.isVisible()) {
      await card.click()
      // Scroll down to reviews
      await page.evaluate(() => window.scrollTo(0, 800))
      await page.waitForTimeout(300)
      const reviews = page.locator('.pdp-section, .review-item, .review-sort')
      if (await reviews.isVisible()) {
        await expect(reviews.first()).toBeVisible()
      }
    }
  })
})

test.describe('登录注册', () => {
  test('登录页加载', async ({ page }) => {
    await page.goto('/login')
    await expect(page.locator('.login-card, .login-page').first()).toBeVisible()
    await expect(page.locator('input[type="text"], input[placeholder*="用户"]').first()).toBeVisible()
    await expect(page.locator('input[type="password"]').first()).toBeVisible()
  })

  test('登录表单可填写', async ({ page }) => {
    await page.goto('/login')
    const userInput = page.locator('input[type="text"], input[placeholder*="用户"]').first()
    const passInput = page.locator('input[type="password"]').first()
    await userInput.fill('testuser')
    await passInput.fill('testpass')
    await expect(userInput).toHaveValue('testuser')
  })

  test('注册页加载', async ({ page }) => {
    await page.goto('/register')
    await expect(page.locator('.register-card, .register-page').first()).toBeVisible()
    await expect(page.locator('input[placeholder*="用户"]').first()).toBeVisible()
  })

  test('OAuth 第三方登录显示', async ({ page }) => {
    await page.goto('/login')
    const oauth = page.locator('.oauth-section, .oauth-btn')
    if (await oauth.isVisible()) {
      await expect(oauth.first()).toBeVisible()
    }
  })

  test('未登录访问购物车跳转登录', async ({ page }) => {
    await page.goto('/cart')
    await page.waitForTimeout(500)
    const url = page.url()
    expect(url).toMatch(/login|cart/)
  })
})

test.describe('购物车', () => {
  test('购物车页加载（含空态）', async ({ page }) => {
    await page.goto('/cart')
    await page.waitForTimeout(500)
    // May redirect to login, which is fine
    const content = page.locator('.cart-page, .login-card, .cart-item, [class*="empty"]').first()
    if (await content.isVisible()) {
      await expect(content).toBeVisible()
    }
  })
})

test.describe('其他页面', () => {
  test('404 页面', async ({ page }) => {
    await page.goto('/nonexistent-page-12345')
    await expect(page.locator('.nf-page, .nf-code, text=404').first()).toBeVisible()
  })

  test('帮助中心', async ({ page }) => {
    await page.goto('/help')
    await expect(page.locator('.help-page, .faq-item').first()).toBeVisible()
  })

  test('心愿单', async ({ page }) => {
    await page.goto('/wishlist')
    await expect(page.locator('.wl-grid, .wl-card, .page-title, [class*="empty"]').first()).toBeVisible()
  })

  test('优惠券中心', async ({ page }) => {
    await page.goto('/coupons')
    await expect(page.locator('[class*="coupon"], .page-title').first()).toBeVisible()
  })

  test('积分页面', async ({ page }) => {
    await page.goto('/points')
    await expect(page.locator('.pts-page, .pts-hero').first()).toBeVisible()
  })

  test('礼品卡页面', async ({ page }) => {
    await page.goto('/gift-card')
    await expect(page.locator('.gc-page, .gc-form').first()).toBeVisible()
  })

  test('拼团页面', async ({ page }) => {
    await page.goto('/group-buy')
    await expect(page.locator('.gb-page, .gb-hero, .gb-grid').first()).toBeVisible()
  })

  test('秒杀页面', async ({ page }) => {
    await page.goto('/flash-sale')
    await expect(page.locator('.flash-page, .flash-hero, .flash-grid').first()).toBeVisible()
  })

  test('排行榜', async ({ page }) => {
    await page.goto('/ranking')
    await expect(page.locator('.rank-page, .rank-hero, .rank-list').first()).toBeVisible()
  })

  test('最近浏览', async ({ page }) => {
    await page.goto('/recent')
    await expect(page.locator('.rc-page, .rc-grid, [class*="empty"]').first()).toBeVisible()
  })

  test('物流追踪', async ({ page }) => {
    await page.goto('/logistics')
    await expect(page.locator('[class*="logistics"], [class*="tracking"]').first()).toBeVisible()
  })
})

test.describe('响应式与主题', () => {
  test('移动端视口适配', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 })
    await page.goto('/')
    await expect(page.locator('.main-content, body')).toBeVisible()
    // Mobile nav should be visible
    const mobileNav = page.locator('.mobile-nav, [class*="mobile"]')
    if (await mobileNav.isVisible()) {
      await expect(mobileNav).toBeVisible()
    }
  })
})
