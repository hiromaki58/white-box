import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/login.css";

const LoginSucesss: React.FC = () => {
  return(
    <div className="wrapper">
      <Header isLoggedIn={false} />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-in">Login Succeded</h1>
            <a href="../index.html">Back to top page</a>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default LoginSucesss;