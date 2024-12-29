import React, { useEffect, useState } from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/login.css";
import { useNavigate } from "react-router-dom";
const PasswordReset = () => {
    const navigate = useNavigate();
    const [msg, setMsg] = useState("");
    const [emailAddr, setEmailAddr] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [newConfirmPassword, setNewConfirmPassword] = useState("");
    useEffect(() => {
        const emailAddrSessionInfo = sessionStorage.getItem("emailAddr");
        if (emailAddrSessionInfo) {
            setEmailAddr(emailAddrSessionInfo);
        }
    }, []);
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (newPassword !== newConfirmPassword) {
            setMsg("The password is not matched.");
            return;
        }
        try {
            const response = await fetch("/api/player/password-reset", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ newPassword }),
            });
            if (response.ok) {
                navigate("/login/password-reset/success");
            }
            else {
                navigate("/login/password-reset/fail");
            }
        }
        catch (err) {
            setMsg("Please try again later.");
        }
    };
    return (React.createElement("div", { className: "wrapper" },
        React.createElement(Header, null),
        React.createElement("article", { className: "contents" },
            React.createElement("div", { className: "area-login" },
                React.createElement("section", { className: "sec-login" },
                    React.createElement("h1", { className: "sec-login-in" }, "Password reset"),
                    React.createElement("div", { className: "form-reset-in" },
                        React.createElement("div", { className: "form-reset-ttl" }, "E-mail address"),
                        React.createElement("div", { className: "form-reset-mail" }, emailAddr)),
                    React.createElement("form", { className: "form-login", onSubmit: handleSubmit },
                        React.createElement("div", { className: "form-login-in" },
                            React.createElement("div", { className: "form-login-ttl" }, "New password"),
                            React.createElement("input", { className: "form-login-input", type: "password", value: newPassword, onChange: (e) => setNewPassword(e.target.value), required: true, placeholder: "E-mail address" }),
                            React.createElement("br", null)),
                        React.createElement("div", { className: "form-login-in" },
                            React.createElement("div", { className: "form-login-ttl" }, "New password(Re-enter)"),
                            React.createElement("input", { className: "form-login-input", type: "password", value: newConfirmPassword, onChange: (e) => setNewConfirmPassword(e.target.value), required: true, placeholder: "Password" }),
                            React.createElement("br", null)),
                        React.createElement("input", { className: "form-login-button", type: "submit", value: "Save" })),
                    msg && React.createElement("p", null, msg)))),
        React.createElement(Footer, null)));
};
export default PasswordReset;
