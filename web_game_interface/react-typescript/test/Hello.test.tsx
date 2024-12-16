import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, test, expect } from "vitest"; // Vitestの関数をインポート
import Hello from "./Hello";

describe("Hello Component", () => {
  test("renders Hello, World!", () => {
    render(<Hello />);
    const headingElement = screen.getByText("Hello, World!");
    expect(headingElement).toBeInTheDocument();
  });
});
