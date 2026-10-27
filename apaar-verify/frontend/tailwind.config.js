/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        aicte: {
          navy: '#0f2b5c',
          blue: '#1e3a8a',
          accent: '#ea580c',
          gold: '#f59e0b'
        }
      }
    },
  },
  plugins: [],
}
