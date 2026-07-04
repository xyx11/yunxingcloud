import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

const __dirname = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig({
  plugins: [vue()],
  base: '/admin/',
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
        manualChunks(id, { getModuleInfo }) {
          if (!id.includes('node_modules')) return;
          // ECharts 最大，独立拆分（仅仪表盘/大屏使用）
          if (id.includes('echarts') || id.includes('vue-echarts') || id.includes('zrender')) {
            return 'vendor-echarts';
          }
          // Vue + naive-ui 放一起，避免循环依赖
          if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router') ||
              id.includes('naive-ui')) {
            return 'vendor-vue';
          }
          if (id.includes('jsencrypt')) {
            return 'vendor-jsencrypt';
          }
          if (id.includes('axios') || id.includes('js-cookie')) {
            return 'vendor-utils';
          }
          if (id.includes('wangeditor')) {
            return 'vendor-wangeditor';
          }
          if (id.includes('xlsx') || id.includes('jszip')) {
            return 'vendor-xlsx';
          }
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
