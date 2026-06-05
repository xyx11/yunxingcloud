import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 30000,
  retries: 1,
  use: {
    baseURL: 'http://localhost:8080',
    headless: true,
    screenshot: 'only-on-failure',
  },
  webServer: {
    command: 'cd .. && ./mvnw spring-boot:run -pl yunxingcloud-core -Dspring-boot.run.profiles=dev',
    url: 'http://localhost:8080/actuator/health',
    reuseExistingServer: true,
    timeout: 60000,
  },
})
