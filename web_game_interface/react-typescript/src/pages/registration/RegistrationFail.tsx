import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"

const RegistrationFail: React.FC = () => {
  return(
    <div className="wrapper">
      <Header isLoggedIn={false} />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-fail-in">Registration Failure</h1>
            <p className="sec-login-detail">Please try again later</p>
            <a href="../index.html">Back to login page</a>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default RegistrationFail;