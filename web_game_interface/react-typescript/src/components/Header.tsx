import React from "react";
import "../css/base-pc.css";

type HeaderProps = {
  isLoggedIn: boolean;
  onLoginClick: () => void;
};

const Header: React.FC<HeaderProps> = ({ isLoggedIn, onLoginClick }) => {
  return (
    <header className="header">
      <div className="header-content">
        <img src="/images/logo.jpg" alt="Logo" className="logo" />
        <nav>
          <ul>
            {isLoggedIn ? (
              <>
                <li><a href="/profile">Profile</a></li>
                <li><a href="/logout">Logout</a></li>
              </>
            ) : (
              <li><button onClick={onLoginClick}>Login</button></li>
            )}
          </ul>
        </nav>
      </div>
    </header>
  );
};

export default Header;
