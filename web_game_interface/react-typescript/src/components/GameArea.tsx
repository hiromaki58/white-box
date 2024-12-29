import React, { useEffect, useState } from "react";
import { startGame } from "../dist/src/game-program/minesweeper";
import "../css/base-pc.css";

const GameArea: React.FC = () => {
    const [msg, setMsg] = useState("");
    const [gameScore, setGameScore] = useState("");

    const sendGameScore = async (gameScore) => {
        try{
            await fetch("/api/player/post/minesweeper-score", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ gameScore }),
            });
        }
        catch(err){
            setMsg("Fail to update the score");
        }
    };

    useEffect(() => {
        const handleGameScore = (gameScore) => {
            setGameScore(gameScore);
            sendGameScore(gameScore);
        }

        startGame(handleGameScore);

        const fetchGameScore = async () => {
            try{
                const minsweeperScoreResponse = await fetch("/api/player/get/minesweeper-score");
                const minsweeperScore = await minsweeperScoreResponse.json();
                setGameScore(minsweeperScore);
            }
            catch(err){
                setGameScore("N/A")
            }
        };

        fetchGameScore();
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
                        <p className="txt-game-score-in">{gameScore}</p>
                        {msg && <p>{msg}</p>}
                    </div>
                </nav>
            </div>
        </article>
    );
};

export default GameArea;
