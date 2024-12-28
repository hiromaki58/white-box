import React, { useEffect } from "react";
import { startGame } from "../dist/minesweeper";
import "../css/base-pc.css";

const GameArea: React.FC = () => {
    useEffect(() => {
        startGame();
    }, []);

    return (
        <article className="contents">
            <div className="game-contents-left">
                <section>
                    <h4 className="ttl-game">Minesweeper</h4>
                    <div id="gamePivot"></div>
                    <div>
                        <input type="radio" id="pick" name="action" defaultChecked />
                        <label htmlFor="pick">⛏</label> /
                        <input type="radio" id="flag" name="action" />
                        <label htmlFor="flag">🚩</label> /
                        <span id="timer"></span>
                    </div>
                </section>
            </div>
            <div className="game-contents-right">
                <nav className="nav-game">
                    <p className="txt-game-score">Bast score</p>
                    <div className="nav-game-in">
                        <p className="txt-game-score-in">pull the score from DB</p>
                    </div>
                </nav>
            </div>
        </article>
    );
};

export default GameArea;
