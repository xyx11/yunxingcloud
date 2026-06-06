import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '^/(api|login|logout|oauth2|userinfo|\\.well-known)': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        cookieDomainRewrite: { 'localhost:8080': 'localhost:5173' },
      },
    },
  },
  build: {
    outDir: resolve(__dirname, '../yunxingcloud-core/src/main/resources/static'),
    emptyOutDir: true,
  },
})
