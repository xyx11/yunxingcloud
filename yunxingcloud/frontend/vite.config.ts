import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

const __dirname = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '^/(api|oauth2|userinfo|\\.well-known)': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        cookieDomainRewrite: { 'localhost:8080': 'localhost:5173' },
      },
    },
  },
  build: {
    outDir: fileURLToPath(new URL('../yunxingcloud-core/src/main/resources/static', import.meta.url)),
    emptyOutDir: true,
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          'vendor-naive': ['naive-ui'],
          'vendor-echarts': ['echarts', 'vue-echarts'],
        },
      },
    },
  },
  test: {
    globals: true,
    environment: 'happy-dom',
    include: ['src/__tests__/**/*.test.ts'],
    exclude: ['e2e/**', 'node_modules/**'],
  },
})
