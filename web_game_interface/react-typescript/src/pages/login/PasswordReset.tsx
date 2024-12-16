import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/login.css";

const PasswordReset: React.FC = () => {
  return(
    <div className="wrapper">
      <Header />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-in">Password reset</h1>

            <div className="form-reset-in">
              <div className="form-reset-ttl">E-mail address</div>
              <div className="form-reset-mail">michelle.barrett@mail.com</div>
            </div>

            <form className="form-login" action="../admin/login" method="post">
              <div className="form-login-in">
                <div className="form-login-ttl">New password</div>
                <input className="form-login-input" type="text" name="userid" placeholder="E-mail address" /><br />
              </div>

              <div className="form-login-in">
                <div className="form-login-ttl">New password(Re-enter)</div>
                <input className="form-login-input" type="password" name="password" placeholder="Password" /><br />
              </div>

              <input className="form-login-button" type="submit" value="Save" />
            </form>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default PasswordReset;