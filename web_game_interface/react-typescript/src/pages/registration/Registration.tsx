import React, { useState } from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/registration.css"
import { useNavigate } from "react-router-dom";

const Registration: React.FC = () => {
    const navigate = useNavigate();
    const [msg, setMsg] = useState("");
    const [firstName, setFirstName] = useState("");
    const [familyName, setFamilyName] = useState("");
    const [emailAddr, setEmailAddr] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try{
            const response = await fetch("/api/player/registration",{
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstName, familyName, emailAddr }),
            });

            if(response.ok){
                navigate("/registration/success");
            }
            else{
                navigate("/registration/fail");
            }
        }
        catch(err){
            setMsg("Please try again later.");
        }
    }

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
                    <form className="form-register-player" onSubmit={handleSubmit}>
                        <section className="sec-register-player-01">
                            <h2 className="ttl-register-player">Information</h2>
                            <ul className="form-cmn-01-wrap">
                                <li className="form-cmn-01">
                                    <p className="form-registrtion-ttl-01">First name</p>
                                    <input className="form-registrtion-input-01" type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="First name" />
                                </li>
                                <li className="form-cmn-01">
                                    <p className="form-registrtion-ttl-01">Family name</p>
                                    <input className="form-registrtion-input-01" type="text" value={familyName} onChange={(e) => setFamilyName(e.target.value)} placeholder="Family name" />
                                </li>
                            </ul>
                            <ul className="form-cmn-01-wrap">
                                <li className="form-cmn-01">
                                    <p className="form-registrtion-ttl-02">E-mail address</p>
                                    <input className="form-registrtion-input-04" type="text" value={emailAddr} onChange={(e) => setEmailAddr(e.target.value)} placeholder="E-mail address" />
                                </li>
                            </ul>
                        </section>
                        <div className="btn-registration-player">
                            <input className="btn-registration-player-01" type="submit" value="registration" />
                        </div>
                    </form>
                    {msg && <p>{msg}</p>}
                </div>
            </article>
            <Footer />
        </div>
    );
};

export default Registration;