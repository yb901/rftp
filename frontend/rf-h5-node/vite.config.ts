import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

const APP_CDN_NAME = 'rf-h5';

const buildAppCdnBaseUrl = (value?: string) => {
  if (!value) return '/';
  const cdnBaseUrl = value.endsWith('/') ? value.slice(0, -1) : value;
  return `${cdnBaseUrl}/${APP_CDN_NAME}/`;
};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const cdnBaseUrl = buildAppCdnBaseUrl(env.CDN_BASE_URL);

  return {
    base: cdnBaseUrl,
    envPrefix: ['VITE_', 'H5_', 'CDN_'],
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
