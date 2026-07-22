import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

const plugins: Plugin[] = [vue()]

// Optional bundle analyzer - install: npm i -D rollup-plugin-visualizer, then run: ANALYZE=1 npm run build
if (process.env.ANALYZE) {
  try {
    const { visualizer } = require('rollup-plugin-visualizer')
    plugins.push(visualizer({ filename: 'dist/stats.html', open: false, gzipSize: true, brotliSize: true }))
  } catch { /* optional dep not installed */ }
}

export default defineConfig({
  plugins,
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    port: 5174,
    proxy: {
      '/api': { target: 'http://localhost:8090', changeOrigin: true },
    },
  },
  base: '/',
  build: {
    outDir: 'dist',
    target: 'es2020',
    cssCodeSplit: true,
    cssMinify: true,
    minify: 'esbuild',
    chunkSizeWarningLimit: 500,
    reportCompressedSize: true,
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id: string) {
          if (id.includes('node_modules/vue') || id.includes('node_modules/vue-router') || id.includes('node_modules/pinia')) return 'vendor-vue'
          if (id.includes('node_modules/naive-ui')) return 'vendor-ui'
          if (id.includes('node_modules/axios')) return 'vendor-axios'
          if (id.includes('node_modules/@vue') || id.includes('node_modules/vue-')) return 'vendor-vue'
        },
      },
    },
  },
})
