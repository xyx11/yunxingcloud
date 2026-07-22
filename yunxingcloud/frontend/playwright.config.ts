import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 30000,
  retries: 1,
  use: {
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
  projects: [
    { name: 'admin', use: { browserName: 'chromium', baseURL: 'http://localhost:8080' } },
    { name: 'mall', use: { browserName: 'chromium', baseURL: 'http://localhost:5174' } },
  ],
  // Run admin tests by default, use --project=mall for mall tests
  reporter: [['list'], ['html', { open: 'never' }]],
})
