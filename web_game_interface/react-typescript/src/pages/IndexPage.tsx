import React from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import GameCard from "../components/GameCard";

const HomePage: React.FC = () => {
  return (
    <div className="wrapper">
      <Header isLoggedIn={false} onLoginClick={() => alert("Login clicked!")} />
      <GameCard
        linkTo="/game/minesweeper"
      />
      <Footer />
    </div>
  );
};

export default HomePage;
