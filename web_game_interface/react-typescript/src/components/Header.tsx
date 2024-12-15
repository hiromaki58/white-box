import React from "react";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";
import "../css/base-pc.css";

type HeaderProps = {
  isLoggedIn: boolean;
};

const Header: React.FC<HeaderProps> = () => {
  const { isLoggedIn } = useAuth();

  return (
    <header className="header">
      <div className="header-in only-sp">
        <section>
          <h1 className="header-logo logo-company">
            <a href="https://"><img src="/img/logo_institution_01.jpg" alt="https://"/></a>
          </h1>
          <nav className="nav-global">
          <ul>
              {isLoggedIn ? (
                <>
                  <li><Link to="/logout"><span className="nav-global-in-first">logout</span></Link></li>
                  <li><a href="#"><span className="nav-global-in">profile</span></a></li>
                </>
              ) : (
                <>
                  <li><Link to="/login"><span className="nav-global-in-first">login</span></Link></li>
                  <li><a href="#"><span className="nav-global-in">registration</span></a></li>
                </>
              )}
            </ul>
          </nav>
        </section>
      </div>

      <div className="header-in only-pc clearfix">
        <section>
          <h1 className="header-logo logo-company">
            <a href="https://"><img src="/img/logo_institution_01.jpg" alt="https:///" /></a>
          </h1>
          <nav className="nav-global">
            <ul>
              {isLoggedIn ? (
                <>
                  <li><Link to="/logout"><span className="nav-global-in-first">logout</span></Link></li>
                  <li><a href="#"><span className="nav-global-in">profile</span></a></li>
                </>
              ) : (
                <>
                  <li><Link to="/login"><span className="nav-global-in-first">login</span></Link></li>
                  <li><a href="#"><span className="nav-global-in">registration</span></a></li>
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
