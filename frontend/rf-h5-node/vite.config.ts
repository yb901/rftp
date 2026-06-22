import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

const buildAppCdnBaseUrl = (value?: string) => {
  if (!value) return '/';
  return value.endsWith('/') ? value : `${value}/`;
};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const cdnBaseUrl = buildAppCdnBaseUrl(env.APP_CDN_BASE_URL);

  return {
    base: cdnBaseUrl,
    envPrefix: ['VITE_', 'APP_'],
    plugins: [react()],
    server: {
      host: '0.0.0.0',
      port: 18094,
      proxy: {
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:18093',
          changeOrigin: true,
        },
      },
    },
  };
});
