import React from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import GameCard from "../components/GameCard";

const HomePage: React.FC = () => {
  const handlePlayClick = (game: string) => {
    console.log(`Starting game: ${game}`);
  };

  return (
    <div className="wrapper">
      <Header isLoggedIn={false} onLoginClick={() => alert("Login clicked!")} />
        <GameCard
          title="Minesweeper"
          image="/images/minesweeper.jpg"
          onPlayClick={() => handlePlayClick("Minesweeper")}
        />
      <Footer />
    </div>
  );
};

export default HomePage;
