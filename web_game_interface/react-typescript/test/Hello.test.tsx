import React from "react";
import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import Hello from "./Hello";

test("renders Hello, World!", () => {
  render(<Hello />);
  const headingElement = screen.getByText("Hello, World!");
  expect(headingElement).toBeInTheDocument();
});
