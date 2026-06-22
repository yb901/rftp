import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

const APP_CDN_NAME = 'rf-mng-node';

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
    envPrefix: ['VITE_', 'MNG_', 'CDN_'],
    plugins: [react()],
    server: {
      port: 18092,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:18091',
          changeOrigin: true,
        },
      },
    },
  };
});
