import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

const buildAppCdnBaseUrl = (value?: string) => {
  if (!value) return '/';
  return value.endsWith('/') ? value : `${value}/`;
};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const cdnBaseUrl = buildAppCdnBaseUrl(env.CDN_BASE_URL);

  return {
    base: cdnBaseUrl,
    envPrefix: ['VITE_', 'API_', 'CDN_'],
    plugins: [react()],
    server: {
      port: 18092,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:18091',
          changeOrigin: true,
        },
        '/mng': {
          target: 'http://127.0.0.1:18091',
          changeOrigin: true,
        },
      },
    },
  };
});
