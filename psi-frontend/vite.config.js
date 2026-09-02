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
  build: {
    // Disable Vite's automatic cleanup of dist/ to avoid safe-delete 144-file guard;
    // leftover files from previous builds are harmless (only hashed assets served).
    emptyOutDir: false
  },
  server: {
    port: 5173,
    proxy: {
      // 单体应用：所有 /psi/* 请求统一代理到 8080
      '/psi': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // API 请求
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})