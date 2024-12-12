import React from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import "../css/base-pc.css";
import "../css/login.css"

const LoginPage: React.FC = () => {
  return (
    <div className="wrapper">
      <Header isLoggedIn={false} />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-in">Login</h1>

            <form className="form-login" action="j_security_check" method="post">
              <div className="form-login-in">
                <div className="form-login-ttl">E-mail address</div>
                <input className="form-login-input" type="text" name="j_username" placeholder="E-mail" /><br />
              </div>

              <div className="form-login-in">
                  <div className="form-login-ttl">Password</div>
                <input className="form-login-input" type="password" name="j_password" placeholder="password"/><br/>
              </div>

              <input className="form-login-button" type="submit" value="login" />
            </form>
            <a href="./reissue.html">If you forget password</a>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default LoginPage;
