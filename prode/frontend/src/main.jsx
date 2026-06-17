import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router";
import App from "./App.jsx";
import AdminDashboard from "./pages/admin/AdminDashboard.jsx";
import AdminTournamentDetailPage from "./pages/admin/AdminTournamentDetailPage.jsx";
import AdminTournamentResultsPage from "./pages/admin/AdminTournamentResultsPage.jsx";
import AdminTournamentSchedulePage from "./pages/admin/AdminTournamentSchedulePage.jsx";
import AdminTournamentTeamsPage from "./pages/admin/AdminTournamentTeamsPage.jsx";
import AdminTournamentsPage from "./pages/admin/AdminTournamentsPage.jsx";
import AdminTeamsPage from "./pages/admin/AdminTeamsPage.jsx";
import Registro from "./pages/Registro.jsx";
import Login from "./pages/Login.jsx";
import MatchesPage from "./pages/MatchesPage.jsx";
import PredictionsPage from "./pages/PredictionsPage.jsx";
import UserDashboard from "./pages/UserDashboard.jsx";
import RankingPage from "./pages/RankingPage.jsx";
import TournamentHomePage from "./pages/TournamentHomePage.jsx";
import TournamentMatchesPage from "./pages/TournamentMatchesPage.jsx";
import TournamentPredictionsPage from "./pages/TournamentPredictionsPage.jsx";
import TournamentRankingPage from "./pages/TournamentRankingPage.jsx";
import TournamentsPage from "./pages/TournamentsPage.jsx";
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
        <Route path="/tournaments" element={<TournamentsPage />} />
        <Route path="/tournaments/:tournamentId" element={<TournamentHomePage />} />
        <Route
          path="/tournaments/:tournamentId/matches"
          element={<TournamentMatchesPage />}
        />
        <Route
          path="/tournaments/:tournamentId/predictions"
          element={<TournamentPredictionsPage />}
        />
        <Route
          path="/tournaments/:tournamentId/ranking"
          element={<TournamentRankingPage />}
        />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/tournaments" element={<AdminTournamentsPage />} />
        <Route
          path="/admin/tournaments/:tournamentId"
          element={<AdminTournamentDetailPage />}
        />
        <Route
          path="/admin/tournaments/:tournamentId/teams"
          element={<AdminTournamentTeamsPage />}
        />
        <Route
          path="/admin/tournaments/:tournamentId/schedule"
          element={<AdminTournamentSchedulePage />}
        />
        <Route
          path="/admin/tournaments/:tournamentId/results"
          element={<AdminTournamentResultsPage />}
        />
        <Route path="/admin/teams" element={<AdminTeamsPage />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
);
