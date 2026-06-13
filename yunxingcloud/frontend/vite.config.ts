import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

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
      '^/(api|login|logout|oauth2|userinfo|\\.well-known)': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        cookieDomainRewrite: { 'localhost:8090': 'localhost:5173' },
      },
    },
  },
  build: {
    outDir: fileURLToPath(new URL('../yunxingcloud-core/src/main/resources/static', import.meta.url)),
    emptyOutDir: true,
  },
})
