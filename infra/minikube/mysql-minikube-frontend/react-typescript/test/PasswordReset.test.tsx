import React from "react";
import { vi } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";

import PasswordReset from "../src/pages/login/PasswordReset";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../src/context/AuthContext";

describe("Password reset component", () => {
    beforeEach(() => {
        global.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => ({ success: true }),
        });
    });

    const mockLogin = vi.fn();
    const mockLogout = vi.fn();
    vi.mock("react-router-dom", () => ({
        ...vi.importActual("react-router-dom"),
        Link: ({ to, children }: { to: string; children: React.ReactNode }) => (
            <a href={to}>{children}</a>
        ),
        useNavigate: vi.fn(),
    }));

    afterEach(() => {
        vi.resetAllMocks();
    });

    test("succed to updating password", async () => {
        const mockNavigate = vi.fn();
        (useNavigate as jest.Mock).mockReturnValue(mockNavigate);

        // mock the fetch
        const mockFetch = vi.spyOn(global, "fetch").mockResolvedValue({
            ok: true,
            json: async () => ({ success: true }),
        } as Response);

        render(
            <AuthContext.Provider value={{ isLoggedIn: false, login: mockLogin, logout: mockLogout, }}>
                <PasswordReset />
            </AuthContext.Provider>
        );

        // set face passwords
        fireEvent.change(screen.getByPlaceholderText(/e-mail address/i), {
            target: { value: "newPassword123" },
        });
        fireEvent.change(screen.getByPlaceholderText(/password/i), {
            target: { value: "newPassword123" },
        });

        // click the save button
        fireEvent.click(screen.getByRole("button", { name: /save/i }));

        // right api response
        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith("/api/player/password-reset", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ newPassword: "newPassword123" }),
            });
        });

        expect(mockNavigate).toHaveBeenCalledWith("/login/password-reset/success");

        // clear the mock
        mockFetch.mockRestore();
        vi.restoreAllMocks();
    });
});