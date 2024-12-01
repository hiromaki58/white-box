import React from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";

const GamePage: React.FC = () => {
  return (
    <div className="wrapper">
      <Header isLoggedIn={true} onLoginClick={() => {}} />
      <main>
        <h1>Minesweeper</h1>
        <div id="game-container">
          {/* ゲームロジックの組み込み */}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default GamePage;
