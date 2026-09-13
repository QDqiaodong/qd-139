import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8139,
    proxy: {
      '/api': {
        target: 'http://localhost:8149',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist'
  }
})