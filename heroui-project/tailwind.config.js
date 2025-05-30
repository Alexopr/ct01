import { heroui } from "@heroui/react";

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
    "./node_modules/@heroui/theme/dist/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  darkMode: "class",
  plugins: [
    heroui({
      layout: {
        dividerWeight: "1px", 
        disabledOpacity: 0.45, 
        fontSize: {
          tiny: "0.75rem",
          small: "0.875rem",
          medium: "0.9375rem",
          large: "1.125rem",
        },
        lineHeight: {
          tiny: "1rem", 
          small: "1.25rem", 
          medium: "1.5rem", 
          large: "1.75rem", 
        },
        radius: {
          small: "6px", 
          medium: "8px", 
          large: "12px", 
        },
        borderWidth: {
          small: "1px", 
          medium: "1px", 
          large: "2px", 
        },
      },
      themes: {
        dark: {
          colors: {
            background: {
              DEFAULT: "#0a0a0a"
            },
            content1: {
              DEFAULT: "#121212",
              foreground: "#e4e4e7"
            },
            content2: {
              DEFAULT: "#1a1a1a",
              foreground: "#e4e4e7"
            },
            content3: {
              DEFAULT: "#222222",
              foreground: "#e4e4e7"
            },
            content4: {
              DEFAULT: "#2a2a2a",
              foreground: "#e4e4e7"
            },
            divider: {
              DEFAULT: "rgba(255, 255, 255, 0.08)"
            },
            primary: {
              DEFAULT: "#3b82f6",
              foreground: "#ffffff"
            }
          }
        }
      }
    })
  ]
};