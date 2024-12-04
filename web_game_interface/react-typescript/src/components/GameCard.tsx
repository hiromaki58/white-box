import React from "react";
import "../css/base-pc.css";

type GameCardProps = {
  title: string;
  image: string;
  onPlayClick: () => void;
};

const GameCard: React.FC<GameCardProps> = ({ title, image, onPlayClick }) => {
  return (
    <div className="contents">
      <div className="area-game">
        <div className="sec-game">
          <section>
            <h2 className="ttl-game-01">Game List</h2>
            <h3 className="ttl-game-02">Login function allows to save the score</h3>
            <div className="sec-game-in">
              <ul className="box-game-wrap">
                <li className="box-game">
                  <ul className="box-game-in">

                    <li className="mod-game-wrap">
                      <a href="#">
                        <div className="img-game"></div>
                      </a>
                      <section>
                        <a href="#">
                          <h4 className="ttl-game-individuality">Minesweeper</h4>
                        </a>
                        <div className="btn-game-wrap">
                          <a href="#" className="btn-game-02">
                            <span className="btn-game-detail">Play</span>
                          </a>
                        </div>
                      </section>
                    </li>

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
