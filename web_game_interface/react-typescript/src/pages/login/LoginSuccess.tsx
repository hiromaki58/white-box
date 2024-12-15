import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/login.css";
import { Link } from "react-router-dom";

const LoginSucesss: React.FC = () => {
  return(
    <div className="wrapper">
      <Header isLoggedIn={false} />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-in">Login Succeded</h1>
            <Link to="/">Back to top page</Link>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default LoginSucesss;