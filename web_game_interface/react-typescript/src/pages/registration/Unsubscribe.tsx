import React, { useState } from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"
import { useNavigate, Link } from "react-router-dom";

const Unsubscribe: React.FC = () => {
    const navigate = useNavigate();
    const [msg, setMsg] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        try{
            const response = await fetch("/api/player/delete-account", {
                method: "DELETE",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
            });

            if(response.ok){

                navigate("/");
            }
            else{
                navigate("/unsubscribe/fail");
            }
        }
        catch(err){
            setMsg("Fail to delete the account");
        }
    };

    return(
        <div className="wrapper">
            <Header />
            <article className="contents clearfix">
                <div className="contents-ttl">
                    <section className="sec-ttl">
                        <h1 className="sec-ttl-in">Delete account</h1>
                    </section>
                </div>
                <div className="contents-in">
                    <div className="btn-registration-player">
                        <Link to={"/profile"}>
                            <span className="btb-cmn-positive-01">Back</span>
                        </Link>
                        <span className="btb-cmn-negative-01" onClick={handleSubmit}>Delete</span>
                    </div>
                </div>
                {msg && <p>{msg}</p>}
            </article>
            <Footer />
        </div>
    );
};

export default Unsubscribe;