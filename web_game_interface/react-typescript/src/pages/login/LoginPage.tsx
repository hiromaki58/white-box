import React, { useState } from "react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import "../../css/base-pc.css";
import "../../css/login.css"

import { useNavigate } from "react-router-dom";
import { useAuthProvider } from "../../context/AuthContext";

const LoginPage: React.FC = () => {
    const navigate = useNavigate();
    const { login } = useAuthProvider();
    const [emailAddr, setEmailAddr] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        try{
            await login(emailAddr, password);
            navigate("/login/success");
        }
        catch(err){
            navigate("/login/fail");
        }
    };

    return (
        <div className="wrapper">
            <Header />
            <article className="contents">
                <div className="area-cmn-01">
                    <section className="sec-cmn-01">
                        <h1 className="sec-cmn-in-01">Login</h1>
                        <form className="form-login" onSubmit={handleSubmit}>
                            <div className="form-login-in">
                                <div className="form-login-ttl">E-mail address</div>
                                <input className="form-login-input" type="email" id="email" value={emailAddr} onChange={(e) => setEmailAddr(e.target.value)} required placeholder="E-mail" /><br />
                            </div>

                            <div className="form-login-in">
                                <div className="form-login-ttl">Password</div>
                                <input className="form-login-input" type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)} required placeholder="password"/><br/>
                            </div>

                            <input className="form-login-button" type="submit" value="login" />
                        </form>
                        <a href="./reissue.html">If you forget password</a>
                    </section>
                </div>
            </article>
            <Footer />
        </div>
    );
};

export default LoginPage;
