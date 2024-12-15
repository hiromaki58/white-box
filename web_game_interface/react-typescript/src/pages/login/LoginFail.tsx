import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/login.css";

const LoginFail: React.FC = () => {
  return(
    <div className="wrapper">
      <Header isLoggedIn={false} />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-fail-in">Login Failure</h1>
            <p className="sec-login-detail">Please check your e-mail and password</p>
            <a href="../index.html">Back to login page</a>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default LoginFail;