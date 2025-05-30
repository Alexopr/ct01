import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";
import { HeroUIProvider } from '@heroui/react';

// Force dark theme like Anthropic Console
document.documentElement.classList.add('dark');
document.documentElement.setAttribute('data-theme', 'dark');

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <HeroUIProvider>
      <App />
    </HeroUIProvider>
  </React.StrictMode>
);