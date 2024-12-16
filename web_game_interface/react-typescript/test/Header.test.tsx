import React from "react";
import { describe, test, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { BrowserRouter as Router } from "react-router-dom";
import Header from "../src/components/Header";
import { AuthContext } from "../src/context/AuthContext";

describe("Header Component", () => {
  test("displays Login link when user is not logged in", () => {
    render(
      <AuthContext.Provider value={{ isLoggedIn: false, setIsLoggedIn: () => {} }}>
        <Router>
          <Header />
        </Router>
      </AuthContext.Provider>
    );

    // Try to find "Login" link
    const loginLink = screen.getByText(/login/i);
    expect(loginLink).toBeInTheDocument();
  });

  test("displays Logout link when user is logged in", () => {
    render(
      <AuthContext.Provider value={{ isLoggedIn: true, setIsLoggedIn: () => {} }}>
        <Router>
          <Header />
        </Router>
      </AuthContext.Provider>
    );

    // Try to find "Logout" link
    const logoutLink = screen.getByText(/logout/i);
    expect(logoutLink).toBeInTheDocument();
  });
});
