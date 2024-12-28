import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import "../css/base-pc.css";
import "../css/registration.css";

import { ProfileType } from "../model/Api"

const ProfilePage: React.FC = () => {
    const [msg, setMsg] = useState("");
    const [profileData, setProfileData] = useState<ProfileType | null>(null);

    useEffect(() => {
        const fetchProfile = async () => {
            try{
                const profileResponse = await fetch("/api/player/getProfile");
                const profileJson = await profileResponse.json();

                const profileImgResponse = await fetch("/api/player/getProfileImg");
                const profileImgBlob = await profileImgResponse.blob();

                setProfileData({
                    firstName: profileJson.firstName,
                    familyName: profileJson.familyName,
                    emailAddr: profileJson.emailAddr,
                    profileImg: profileImgBlob
                });
            }
            catch(err){
                setMsg("There are some issues.");
            }
        };

        fetchProfile();
    }, []);

    if(!profileData){
        setMsg("Please access again later.");
    }

    const profileImgUrl = profileData?.profileImg ? URL.createObjectURL(profileData?.profileImg) : "";

    return(
        <div className="wrapper">
            <Header />
            <article className="contents clearfix">
                <div className="profile-contents-ttl">
                    <section>
                        <h2 className="sec-ttl-in">Player info</h2>
                    </section>
                </div>

                <div className="profile-contents-main">
                    <div className="profile-contents-main-main">
                        <section className="sec-player-info-01">
                            <h3 className="ttl-register-player">Profile</h3>
                            <ul className="form-cmn-01-wrap">
                                <li className="list-basic-01">
                                    <p className="ttl-player-info">First name</p>
                                    <span className="txt-player-info">{profileData?.firstName}</span>
                                </li>
                                <li className="list-basic-01">
                                    <p className="ttl-player-info">Last name</p>
                                    <span className="txt-player-info">{profileData?.familyName}</span>
                                </li>
                            </ul>
                            <ul className="form-cmn-01-wrap">
                                <li className="list-basic-02">
                                    <p className="ttl-player-info">E-mail address</p>
                                    <span className="txt-player-info">{profileData?.emailAddr}</span>
                                </li>
                            </ul>
                        </section>
                    </div>
                    <div className="profile-contents-main-sub">
                        <section>
                            <h3 className="ttl-register-player">Image</h3>
                            <div className="ttl-player-img">
                                <div className="form-registration-img">
                                    <img className="img-preview" src={profileImgUrl} alt="profile img" />
                                    <div className="btn-player-img">
                                        <span className="btb-cmn-positive-01">Edit image</span>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <div className="profile-contents-main-bottom">
                        <div className="btn-player-info">
                            <a href="/jwda_01/admin/suspenssion"></a>
                        </div>
                    </div>
                </div>
                {msg && <p>{msg}</p>}
                <div className="profile-contents-sub">
                    <nav className="nav-local">
                        <ul>
                            <li>
                                <h3 className="ttl-register-player">Setting</h3>
                                <ul className="nav-local-in">
                                    <li><a href="/jwda_01/login/reissue.html" className="nav02-01">Change password</a></li>
                                    <li><a href="/jwda_01/login/reissue.html" className="nav02-01">Unsubscribe</a></li>
                                </ul>
                            </li>
                        </ul>
                    </nav>
                </div>
            </article>
            <Footer />
        </div>
    );
};

export default ProfilePage;