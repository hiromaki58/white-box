import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import IndexPage from "./pages/IndexPage";
import LoginPage from "./pages/login/LoginPage";
import LoginSuccessPage from "./pages/login/LoginSuccess";
import LoginFailPage from "./pages/login/LoginFail";
import RegistrationPage from "./pages/registration/Registration";
import RegistrationSuccessPage from "./pages/registration/RegistrationSuccess";
import RegistrationFailPage from "./pages/registration/RegistrationFail";
import GamePage from "./pages/GamePage";
import Header from "./components/Header";

const App: React.FC = () => {
  return (
    <AuthProvider>
      <Router>
        <Header />
        <Routes>
          <Route path="/" element={<IndexPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/login/success" element={<LoginSuccessPage />} />
          <Route path="/login/fail" element={<LoginFailPage />} />
          <Route path="/registration" element={<RegistrationPage />} />
          <Route path="/registration/success" element={<RegistrationSuccessPage />} />
          <Route path="/registration/fail" element={<RegistrationFailPage />} />
          <Route path="/game/minesweeper" element={<GamePage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
