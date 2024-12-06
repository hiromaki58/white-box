import React from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import GameArea from "../components/GameArea";

const GamePage: React.FC = () => {
  return (
    <div className="wrapper">
      <Header isLoggedIn={false} onLoginClick={() => alert("Login clicked!")} />
      <GameArea/>
      <Footer />
    </div>
  );
};

export default GamePage;
