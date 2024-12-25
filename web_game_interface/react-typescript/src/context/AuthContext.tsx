import React, { createContext, useContext, useState, ReactNode } from "react";

type AuthContextType = {
  isLoggedIn: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // To login
  const login = async (email: string, password: string) => {
    try {
      const response = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      const result = await response.json();

      if (result.success) {
        setIsLoggedIn(true);
        sessionStorage.setItem("isLoggedIn", "true");

        // Session time out
        setTimeout(() => {
          sessionStorage.removeItem("isLoggedIn");
          setIsLoggedIn(false);
        }, 60 * 60 * 1000);
      } else {
        throw new Error(result.message || "Login failed");
      }
    } catch (error) {
      console.error("Login error:", error);
    }
  };

  // To logout
  const logout = () => {
    sessionStorage.removeItem("isLoggedIn");
    setIsLoggedIn(false);

    fetch("/api/logout", { method: "POST" }).catch((error) =>
      console.error("Logout error:", error)
    );
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
