import React from "react";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";
import "../css/base-pc.css";

const Header: React.FC = () => {
  const { isLoggedIn } = useAuth();

  return (
    <header className="header">
      <div className="header-in only-sp">
        <section>
          <h1 className="header-logo logo-company">
            <Link to="/"><img src="/img/logo_institution_01.jpg" alt="site logo"/></Link>
          </h1>
          <nav className="nav-global">
          <ul>
              {isLoggedIn ? (
                <>
                  <li><Link to="/logout"><span className="nav-global-in-first">logout</span></Link></li>
                  <li><Link to="/profile"><span className="nav-global-in">profile</span></Link></li>
                </>
              ) : (
                <>
                  <li><Link to="/login"><span className="nav-global-in-first">login</span></Link></li>
                  <li><Link to="/registration"><span className="nav-global-in">registration</span></Link></li>
                </>
              )}
            </ul>
          </nav>
        </section>
      </div>
    </header>
  );
};

export default Header;
