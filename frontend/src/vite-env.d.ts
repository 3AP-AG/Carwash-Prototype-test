/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** The entry page a 401 sends the browser to under `make dev` (vite.config.ts). */
  readonly VITE_ENTRY_PAGE: string;
}
