import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/projects': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/scans': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
})
