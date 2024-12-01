import React from "react";
import "../css/base-pc.css";

const Footer: React.FC = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <p>&copy; 2024 Hiro Maki. All Rights Reserved.</p>
        <ul className="footer-links">
          <li><a href="https://www.linkedin.com/in/hiromaki58/">LinkedIn</a></li>
          <li><a href="https://github.com/hiromaki58/white-box">GitHub</a></li>
        </ul>
      </div>
    </footer>
  );
};

export default Footer;
