import React from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import GameArea from "../components/GameArea";

const GamePage: React.FC = () => {
  const handlePlayClick = (game: string) => {
    console.log(`Starting game: ${game}`);
  };

  return (
    <div className="wrapper">
      <Header isLoggedIn={false} onLoginClick={() => alert("Login clicked!")} />
      <GameArea
        title="Minesweeper"
        image="/images/minesweeper.jpg"
        onPlayClick={() => handlePlayClick("Minesweeper")}
      />
      <Footer />
    </div>
  );
};

export default GamePage;
