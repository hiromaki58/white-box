import React, { createContext, useContext, useEffect, useState, ReactNode } from "react";

type AuthContextType = {
  isLoggedIn: boolean;
  setIsLoggedIn: (value: boolean) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // Spring Bootからセッション情報を取得
    const fetchAuthStatus = async () => {
      try {
        const response = await fetch("/auth/status", {
          credentials: "include", // クッキーを含める
        });
        const result = await response.json();
        setIsLoggedIn(result);
      } catch (error){
        console.error("Error fetching auth status:", error);
      }
    };

    fetchAuthStatus();
  }, []);

  return (
    <AuthContext.Provider value={{ isLoggedIn, setIsLoggedIn }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
