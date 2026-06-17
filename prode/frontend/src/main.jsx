import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router";
import App from "./App.jsx";
import AdminDashboard from "./pages/admin/AdminDashboard.jsx";
import AdminTournamentDetailPage from "./pages/admin/AdminTournamentDetailPage.jsx";
import AdminTournamentsPage from "./pages/admin/AdminTournamentsPage.jsx";
import AdminTeamsPage from "./pages/admin/AdminTeamsPage.jsx";
import Registro from "./pages/Registro.jsx";
import Login from "./pages/Login.jsx";
import MatchesPage from "./pages/MatchesPage.jsx";
import PredictionsPage from "./pages/PredictionsPage.jsx";
import UserDashboard from "./pages/UserDashboard.jsx";
import RankingPage from "./pages/RankingPage.jsx";
import "./index.css";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/registro" element={<Registro />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<UserDashboard />} />
        <Route path="/matches" element={<MatchesPage />} />
        <Route path="/predictions" element={<PredictionsPage />} />
        <Route path="/ranking" element={<RankingPage />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/tournaments" element={<AdminTournamentsPage />} />
        <Route
          path="/admin/tournaments/:tournamentId"
          element={<AdminTournamentDetailPage />}
        />
        <Route path="/admin/teams" element={<AdminTeamsPage />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
);
