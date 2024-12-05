import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import IndexPage from "./pages/IndexPage";
import GamePage from "./pages/GamePage";

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<IndexPage />} />
        <Route path="/game/minesweeper" element={<GamePage />} />
      </Routes>
    </Router>
  );
};

export default App;
