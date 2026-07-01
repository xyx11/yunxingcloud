import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'

export default tseslint.config(
  { ignores: ['dist', 'node_modules'] },
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
        localStorage: 'readonly', sessionStorage: 'readonly',
        window: 'readonly', document: 'readonly', navigator: 'readonly',
        console: 'readonly', fetch: 'readonly',
        setInterval: 'readonly', clearInterval: 'readonly',
        setTimeout: 'readonly', FormData: 'readonly',
        Blob: 'readonly', URLSearchParams: 'readonly', URL: 'readonly'
      }
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      'vue/multi-word-component-names': 'off',
      'vue/max-attributes-per-line': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/attributes-order': 'off',
      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
      'no-undef': 'error',
      'no-empty': 'warn'
    }
  }
)