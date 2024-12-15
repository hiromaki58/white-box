import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import IndexPage from "./pages/IndexPage";
import GamePage from "./pages/GamePage";

const App: React.FC = () => {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<IndexPage />} />
          <Route path="/game/minesweeper" element={<GamePage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
