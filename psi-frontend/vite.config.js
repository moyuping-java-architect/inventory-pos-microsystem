import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8087',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/psi/admin': {
        target: 'http://localhost:8087',
        changeOrigin: true
      },
      '/psi/workflow': {
        target: 'http://localhost:8089',
        changeOrigin: true
      },
      '/psi/sale': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/psi/goods': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/psi/order': {
        target: 'http://localhost:8089',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/psi\/order/, '')
      },
      '/psi/purchase': {
        target: 'http://localhost:8090',
        changeOrigin: true
      },
      '/psi/stock': {
        target: 'http://localhost:8086',
        changeOrigin: true
      },
      '/psi/finance': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/psi/report': {
        target: 'http://localhost:8092',
        changeOrigin: true
      }
    }
  }
})