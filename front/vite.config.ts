import tailwindcss from '@tailwindcss/vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import {defineConfig, loadEnv} from 'vite';

const normalizeBase = (base?: string) => {
  if (!base || base === '/') return '/';
  if (base === '.' || base === './') return './';
  return `/${base.replace(/^\/+|\/+$/g, '')}/`;
};

export default defineConfig(({mode}) => {
  const env = loadEnv(mode, '.', '');
  return {
    base: normalizeBase(env.VITE_PUBLIC_BASE),
    plugins: [vue(), tailwindcss()],
    define: {
      'process.env.GEMINI_API_KEY': JSON.stringify(env.GEMINI_API_KEY),
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, '.'),
      },
    },
    server: {
      port: 3004,
      hmr: process.env.DISABLE_HMR !== 'true',
      fs: {
        allow: [
          path.resolve(__dirname, '.'),
          path.resolve(__dirname, '../res'),
        ],
      },
    },
  };
});
