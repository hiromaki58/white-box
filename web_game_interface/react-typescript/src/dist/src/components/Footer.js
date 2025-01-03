import React from "react";
import "../css/base-pc.css";
const Footer = () => {
    return (React.createElement("footer", { className: "footer" },
        React.createElement("section", null,
            React.createElement("h2", { className: "hidden" }, "footer"),
            React.createElement("div", { className: "footer-in" },
                React.createElement("div", { className: "footer-utility" },
                    React.createElement("div", { className: "footer-utility-in" },
                        React.createElement("div", { className: "footer-logo" },
                            React.createElement("img", { className: "footer-logo-in", src: "/img/logo_institution_01.jpg", alt: "https://" })),
                        React.createElement("section", null,
                            React.createElement("div", { className: "sec-footer-info" },
                                React.createElement("div", { className: "box-info-wrap" },
                                    React.createElement("p", { className: "ttl-info" }, "Portfolio creater")),
                                React.createElement("div", { className: "box-mail-wrap" },
                                    React.createElement("span", { className: "txt-mail" }, "Hiro Maki")))),
                        React.createElement("section", null,
                            React.createElement("div", { className: "sec-footer-info" },
                                React.createElement("div", { className: "box-info-wrap" },
                                    React.createElement("p", { className: "ttl-info" }, "Contact")),
                                React.createElement("div", { className: "box-mail-wrap" },
                                    React.createElement("span", { className: "txt-mail" }, "hiromaki58@gmail.com")))),
                        React.createElement("section", null,
                            React.createElement("div", { className: "box-info-wrap" },
                                React.createElement("p", { className: "ttl-info" }, "SNS")),
                            React.createElement("div", { className: "footer-sns" },
                                React.createElement("ul", null,
                                    React.createElement("li", null,
                                        React.createElement("a", { href: "https://www.linkedin.com/in/hiromaki58/" }, "LinkeIn")),
                                    React.createElement("li", null,
                                        React.createElement("a", { href: "https://github.com/hiromaki58/white-box" }, "Github"))))))),
                React.createElement("p", { className: "footer-copyright" }, " \u00A9 Hiro Maki. All Rights Reserved.")))));
};
export default Footer;
