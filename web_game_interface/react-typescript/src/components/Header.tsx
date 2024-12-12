import React from "react";
import "../css/base-pc.css";

type HeaderProps = {
  isLoggedIn: boolean;
};

const Header: React.FC<HeaderProps> = ({ isLoggedIn }) => {
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
                  <li><a href="#"><span className="nav-global-in-first">logout</span></a></li>
                  <li><a href="#"><span className="nav-global-in">profile</span></a></li>
                </>
              ) : (
                <>
                  <li><a href="#"><span className="nav-global-in-first">login</span></a></li>
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
                  <li><a href="#"><span className="nav-global-in-first">logout</span></a></li>
                  <li><a href="#"><span className="nav-global-in">profile</span></a></li>
                </>
              ) : (
                <>
                  <li><a href="#"><span className="nav-global-in-first">login</span></a></li>
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
