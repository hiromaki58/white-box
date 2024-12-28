import React from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"

const Unsubscribe: React.FC = () => {

    return(
        <div className="wrapper">
            <Header />
            <article className="contents clearfix">
                <div className="contents-ttl">
                    <section className="sec-ttl">
                        <h1 className="sec-ttl-in">Delete play info</h1>
                    </section>
                </div>
                <div className="contents-in">
                    <div className="btn-registration-player">
                        <span className="btb-cmn-positive-01">Back</span>
                        <span className="btb-cmn-negative-01">Delete</span>
                    </div>
                </div>
            </article>
            <Footer />
        </div>
    );
};

export default Unsubscribe;