import React from "react";
import { useAuthProvider } from "../context/AuthContext";
import { Link } from "react-router-dom";
import "../css/base-pc.css";
const Header = () => {
    const { isLoggedIn, logout } = useAuthProvider();
    return (React.createElement("header", { className: "header" },
        React.createElement("div", { className: "header-in only-sp" },
            React.createElement("section", null,
                React.createElement("h1", { className: "header-logo logo-company" },
                    React.createElement(Link, { to: "/" },
                        React.createElement("img", { src: "/img/logo_institution_01.jpg", alt: "site logo" }))),
                React.createElement("nav", { className: "nav-global" },
                    React.createElement("ul", null, isLoggedIn ? (React.createElement(React.Fragment, null,
                        React.createElement("li", { onClick: logout },
                            React.createElement("span", { className: "nav-global-in-first" }, "logout")),
                        React.createElement("li", null,
                            React.createElement(Link, { to: "/profile" },
                                React.createElement("span", { className: "nav-global-in" }, "profile"))))) : (React.createElement(React.Fragment, null,
                        React.createElement("li", null,
                            React.createElement(Link, { to: "/login" },
                                React.createElement("span", { className: "nav-global-in-first" }, "login"))),
                        React.createElement("li", null,
                            React.createElement(Link, { to: "/registration" },
                                React.createElement("span", { className: "nav-global-in" }, "registration")))))))))));
};
export default Header;
