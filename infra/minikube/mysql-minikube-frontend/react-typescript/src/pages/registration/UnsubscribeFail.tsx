import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"
import { Link } from "react-router-dom";

const RegistrationFail: React.FC = () => {
    return (
        <div className="wrapper">
            <Header />
            <article className="contents">
                <div className="area-cmn-01">
                    <section className="sec-cmn-01">
                        <h1 className="sec-cmn-01-fail-in">Cancel Subscription Failure</h1>
                        <p className="sec-cmn-01-detail">Please try again later</p>
                        <Link to="/">Back to login page</Link>
                    </section>
                </div>
            </article>
            <Footer />
        </div>
    );
};

export default RegistrationFail;