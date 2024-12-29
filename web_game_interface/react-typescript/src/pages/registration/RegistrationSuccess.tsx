import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"
import { Link } from "react-router-dom";

const RegistrationSuccess: React.FC = () => {
  return(
    <div className="wrapper">
      <Header />
      <article className="contents">
        <div className="area-cmn-01">
          <section className="sec-cmn-01">
            <h1 className="sec-cmn-in-01">Registration Succeded</h1>
            <Link to="/">Back to top page</Link>
          </section>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default RegistrationSuccess;