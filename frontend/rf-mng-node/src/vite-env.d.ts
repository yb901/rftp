/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly APP_CDN_BASE_URL?: string;
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
