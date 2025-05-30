/** @type {import('tailwindcss').Config} */
const { heroui } = require("@heroui/react");

export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
    "./node_modules/@heroui/theme/dist/**/*.{js,ts,jsx,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        // Anthropic Console style colors с пурпурно-лазурной палитрой
        background: '#0a0a0a',
        foreground: '#ffffff',
        content1: '#111111',
        content2: '#1a1a1a', 
        content3: '#262626',
        content4: '#404040',
        divider: '#262626',
        overlay: 'rgba(0, 0, 0, 0.8)',
        focus: '#8b5cf6',
        // Custom accent colors - пурпурно-лазурная палитра
        'anthropic-green': '#8b5cf6', // Фиолетовый вместо зеленого
        'anthropic-blue': '#06b6d4',  // Лазурный/циан
        'anthropic-purple': '#a855f7', // Пурпурный
        'anthropic-cyan': '#22d3ee',   // Яркий циан
        'anthropic-emerald': '#10b981', // Изумрудный
        'anthropic-gray': '#6b7280',
        'anthropic-dark': '#0f0f0f'
      }
    },
  },
  darkMode: "class",
  plugins: [
    heroui({
      defaultTheme: "dark",
      defaultExtendTheme: "dark",
      themes: {
        dark: {
          colors: {
            background: "#0a0a0a",
            foreground: "#ffffff",
            content1: "#111111",
            content2: "#1a1a1a", 
            content3: "#262626",
            content4: "#404040",
            divider: "#262626",
            overlay: "rgba(0, 0, 0, 0.8)",
            focus: "#22c55e",
            default: {
              50: "#fafafa",
              100: "#f5f5f5",
              200: "#e5e5e5",
              300: "#d4d4d4",
              400: "#a3a3a3",
              500: "#737373",
              600: "#525252",
              700: "#404040",
              800: "#262626",
              900: "#171717",
              foreground: "#ffffff",
              DEFAULT: "#262626",
            },
            primary: {
              50: "#faf5ff",
              100: "#f3e8ff", 
              200: "#e9d5ff",
              300: "#d8b4fe",
              400: "#c084fc",
              500: "#8b5cf6",
              600: "#7c3aed",
              700: "#6d28d9",
              800: "#5b21b6",
              900: "#4c1d95",
              foreground: "#ffffff",
              DEFAULT: "#8b5cf6",
            },
            secondary: {
              50: "#ecfeff",
              100: "#cffafe",
              200: "#a5f3fc", 
              300: "#67e8f9",
              400: "#22d3ee",
              500: "#06b6d4",
              600: "#0891b2",
              700: "#0e7490",
              800: "#155e75",
              900: "#164e63",
              foreground: "#ffffff",
              DEFAULT: "#06b6d4",
            },
            success: {
              DEFAULT: "#10b981",
              foreground: "#ffffff",
            },
            warning: {
              DEFAULT: "#f59e0b",
              foreground: "#ffffff",
            },
            danger: {
              DEFAULT: "#ef4444",
              foreground: "#ffffff",
            },
          },
        },
      },
    }),
  ],
} 