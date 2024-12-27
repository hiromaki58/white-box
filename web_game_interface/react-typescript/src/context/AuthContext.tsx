import React, { createContext, useContext, useState, ReactNode } from "react";
import { AuthContextType } from "src/model/Auth";

export const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    // To login
    const login = async (email: string, password: string) => {
        try {
            const response = await fetch("/api/player/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            const result = await response.json();

            if (result.success) {
                setIsLoggedIn(true);
                sessionStorage.setItem("isLoggedIn", "true");
                sessionStorage.setItem("emailAddr", email);

                // Session time out
                setTimeout(() => {
                sessionStorage.removeItem("isLoggedIn");
                sessionStorage.removeItem("emailAddr");
                setIsLoggedIn(false);
                }, 60 * 60 * 1000);
            } else {
                throw new Error(result.message || "Login failed");
            }
        }
        catch (err) {
            console.error("Login error:", err);
        }
    };

    // To logout
    const logout = () => {
        sessionStorage.removeItem("isLoggedIn");
        setIsLoggedIn(false);

        fetch("/api/player/logout", { method: "POST" }).catch((error) =>
            console.error("Logout error:", error)
        );
    };

    return (
        <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuthProvider = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuthProvider must be used within an AuthProvider");
    }
    return context;
};
