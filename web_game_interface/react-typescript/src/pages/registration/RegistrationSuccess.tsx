import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"
import { Link } from "react-router-dom";

const RegistrationSuccess: React.FC = () => {
  return(
    <div className="wrapper">
      <Header isLoggedIn={false} />
      <article className="contents">
        <div className="area-login">
          <section className="sec-login">
            <h1 className="sec-login-in">Registration Succeded</h1>
            <Link to="/">Back to top page</Link>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default RegistrationSuccess;