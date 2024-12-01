import React from "react";
import "../css/base-pc.css";

type GameCardProps = {
  title: string;
  image: string;
  onPlayClick: () => void;
};

const GameCard: React.FC<GameCardProps> = ({ title, image, onPlayClick }) => {
  return (
    <div className="game-card">
      <img src={image} alt={title} className="game-image" />
      <h3>{title}</h3>
      <button onClick={onPlayClick}>Play</button>
    </div>
  );
};

export default GameCard;
