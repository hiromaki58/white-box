import React from "react";
import "../css/base-pc.css";

type GameAreaProps = {
  title: string;
  image: string;
  onPlayClick: () => void;
};

const GameArea: React.FC<GameAreaProps> = ({ title, image, onPlayClick }) => {
  return (
    <div className="contents">
      <div className="area-game">
        
      </div>
    </div>
  );
};

export default GameArea;
