import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'

export default tseslint.config(
  { ignores: ['dist', 'node_modules', '../yunxingcloud-core/src/main/resources/static'] },
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
      globals: { localStorage: 'readonly', sessionStorage: 'readonly', window: 'readonly', document: 'readonly', navigator: 'readonly', console: 'readonly', fetch: 'readonly', EventSource: 'readonly', CustomEvent: 'readonly', EventListener: 'readonly', setInterval: 'readonly', clearInterval: 'readonly', setTimeout: 'readonly', KeyboardEvent: 'readonly', HTMLElement: 'readonly', HTMLFormElement: 'readonly', URLSearchParams: 'readonly', URL: 'readonly', FormData: 'readonly', Blob: 'readonly' }
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      'vue/multi-word-component-names': 'off',
      'vue/max-attributes-per-line': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/attributes-order': 'off',
      'no-unused-vars': 'warn',
      '@typescript-eslint/no-unused-vars': 'warn',
      'no-undef': 'error',
      'no-empty': 'warn'
    }
  }
)
