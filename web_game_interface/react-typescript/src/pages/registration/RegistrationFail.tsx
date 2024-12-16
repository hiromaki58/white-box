import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"
import { Link } from "react-router-dom";

const RegistrationFail: React.FC = () => {
  return(
    <div className="wrapper">
      <Header />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-fail-in">Registration Failure</h1>
            <p className="sec-login-detail">Please try again later</p>
            <Link to="/">Back to login page</Link>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default RegistrationFail;