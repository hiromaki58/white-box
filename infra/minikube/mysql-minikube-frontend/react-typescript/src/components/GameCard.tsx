import React from "react";
import "../css/base-pc.css";
import { Link } from "react-router-dom";

type GameCardProps = {
    linkTo: string;
};

const GameCard: React.FC<GameCardProps> = ({ linkTo }) => {
    return (
        <div className="contents">
            <div className="area-game">
                <div className="sec-game">
                    <section>
                        <h2 className="ttl-game-h2">Game List</h2>
                        <h3 className="ttl-game-h3">Login function allows to save the score</h3>
                        <div className="sec-game-in">
                            <ul className="box-game-wrap">
                                <li className="box-game">
                                    <ul className="box-game-in">
                                        <Link to={linkTo}>
                                            <li className="mod-game-wrap">
                                                <div className="img-game"></div>
                                                <section>
                                                    <h4 className="ttl-game-individuality">Minesweeper</h4>
                                                    <div className="btn-game">
                                                        <span className="btn-game-detail">Play</span>
                                                    </div>
                                                </section>
                                            </li>
                                        </Link>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </section>
                </div>
            </div>
        </div>
    );
};

export default GameCard;
