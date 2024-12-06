import React, { useEffect } from "react";
import { startGame } from "../dist/minesweeper";
import "../css/base-pc.css";

const GameArea: React.FC = () => {
  useEffect(() => {
    startGame();
  }, []);

  return (
    <div className="contents">
      <div className="area-game">
        <div id="gamePivot" style={{ position: "relative" }}></div>
        <div style={{ fontSize: "30px" }}>
          <input type="radio" id="pick" name="action" defaultChecked />
          <label htmlFor="pick">⛏</label> /
          <input type="radio" id="flag" name="action" />
          <label htmlFor="flag">🚩</label> /
          <span id="timer"></span>
        </div>
      </div>
    </div>
  );
};

export default GameArea;
