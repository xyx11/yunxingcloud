import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'

export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'public', 'vite.config.ts', 'vitest.config.ts', 'playwright.config.ts'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    files: ['*.vue', '**/*.vue'],
    languageOptions: {
      parserOptions: { parser: tseslint.parser }
    }
  },
  {
    languageOptions: {
      globals: {
        localStorage: 'readonly', sessionStorage: 'readonly', Storage: 'readonly',
        window: 'readonly', document: 'readonly', navigator: 'readonly',
        console: 'readonly', fetch: 'readonly',
        setInterval: 'readonly', clearInterval: 'readonly',
        setTimeout: 'readonly', requestAnimationFrame: 'readonly',
        FormData: 'readonly', Blob: 'readonly', URLSearchParams: 'readonly', URL: 'readonly',
        Event: 'readonly', CustomEvent: 'readonly', MouseEvent: 'readonly',
        KeyboardEvent: 'readonly', TouchEvent: 'readonly',
        HTMLElement: 'readonly', HTMLInputElement: 'readonly', HTMLTextAreaElement: 'readonly',
        IntersectionObserver: 'readonly', MutationObserver: 'readonly',
        AbortController: 'readonly', PerformanceObserver: 'readonly',
        clearTimeout: 'readonly', location: 'readonly',
        File: 'readonly', Notification: 'readonly',
        atob: 'readonly', confirm: 'readonly',
      }
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      'vue/multi-word-component-names': 'off',
      'vue/max-attributes-per-line': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/attributes-order': 'off',
      'vue/html-indent': 'off',
      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
      'no-undef': 'error',
      'vue/html-self-closing': 'off',
      'vue/html-closing-bracket-spacing': 'off',
      'vue/no-template-shadow': 'off',
      'vue/valid-v-on': 'off',
      'vue/multiline-html-element-content-newline': 'off',
      'vue/first-attribute-linebreak': 'off',
      'vue/html-closing-bracket-newline': 'off',
      'vue/require-default-prop': 'off',
      'vue/no-v-html': 'off',
      'no-useless-catch': 'off',
      'no-empty': ['warn', { allowEmptyCatch: true }],
      '@typescript-eslint/no-unused-expressions': ['warn', { allowTernary: true }],
    }
  }
)