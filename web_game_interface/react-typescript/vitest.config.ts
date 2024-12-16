import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    globals: true, // expectやtestをグローバルに使用可能にする
    environment: "jsdom",
    setupFiles: "./vitest.setup.ts",
  },
});
