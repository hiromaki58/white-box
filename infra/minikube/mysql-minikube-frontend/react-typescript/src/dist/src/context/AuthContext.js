import React, { createContext, useContext, useState } from "react";
export const AuthContext = createContext(null);
export const AuthProvider = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    // To login
    const login = async (emailAddr, password) => {
        try {
            const response = await fetch("/api/player/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ emailAddr, password }),
            });
            const result = await response.json();
            if (result.success) {
                setIsLoggedIn(true);
                sessionStorage.setItem("isLoggedIn", "true");
                sessionStorage.setItem("emailAddr", emailAddr);
                // Session time out
                setTimeout(() => {
                    sessionStorage.removeItem("isLoggedIn");
                    sessionStorage.removeItem("emailAddr");
                    setIsLoggedIn(false);
                }, 60 * 60 * 1000);
            }
            else {
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
        fetch("/api/player/logout", { method: "POST" }).catch((err) => console.error("Logout error:", err));
    };
    return (React.createElement(AuthContext.Provider, { value: { isLoggedIn, login, logout } }, children));
};
export const useAuthProvider = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuthProvider must be used within an AuthProvider");
    }
    return context;
};
