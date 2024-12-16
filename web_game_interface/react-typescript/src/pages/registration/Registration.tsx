import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"

const Registration: React.FC = () => {
  return(
    <div className="wrapper">
      <Header />
      <article className="contents clearfix">
        <div className="contents-ttl">
          <section className="sec-ttl">
            <h1 className="sec-ttl-in">Player registration</h1>
          </section>
        </div>
        <div className="contents-in">
          <form className="form-register-player" action="" method="post">
            <section className="sec-register-player-01">
              <h2 className="ttl-register-player">Information</h2>

              <ul className="form-cmn-01-wrap">
                <li className="form-cmn-01">
                  <p className="form-registrtion-ttl-01">First name</p>
                  <input className="form-registrtion-input-01" type="text" id="instructor_first_name" name="instructor_first_name" placeholder="First name" />
                </li>
                <li className="form-cmn-01">
                  <p className="form-registrtion-ttl-01">Family name</p>
                  <input className="form-registrtion-input-01" type="text" id="instructor_last_name" name="instructor_last_name" placeholder="Family name" />
                </li>
              </ul>

              <ul className="form-cmn-01-wrap">
                <li className="form-cmn-01">
                  <p className="form-registrtion-ttl-02">E-mail address</p>
                  <input className="form-registrtion-input-04" type="text" name="instructor_mail" placeholder="E-mail address" />
                </li>
              </ul>
            </section>

            <div className="btn-registration-player">
              <input className="btn-registration-player-01" type="submit" value="registration" />
            </div>
          </form>
        </div>
      </article>
      <Footer />
    </div>
  );
};

export default Registration;