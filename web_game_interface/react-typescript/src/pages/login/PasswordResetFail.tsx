import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/login.css";
import { Link } from "react-router-dom";

const PasswordResetFail: React.FC = () => {
  return(
    <div className="wrapper">
      <Header isLoggedIn={false} />
      <article className="contents">
        <div className="area-reset">
          <section className="sec-login">
            <h1 className="sec-login-in">Password reset error</h1>
              <div className="form-reset-in">
                <p className="form-reset-ttl">The e-mail address is no registered.</p>
              </div>
              <div className="btn-registration-password">
                <Link to="/">
                  <span className="btn-registration-password-01">Back to top page</span>
                </Link>
              </div>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default PasswordResetFail;