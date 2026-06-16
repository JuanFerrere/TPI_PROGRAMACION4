import { useState } from "react";
import { guardarPronostico } from "../../services/predictionService.js";
import Button from "../ui/Button.jsx";
import ErrorMessage from "../ui/ErrorMessage.jsx";

function calcularTendencia(local, visitante, homeTeamName, awayTeamName) {
  if (local > visitante) {
    return `Gana ${homeTeamName || "Equipo local"}`;
  }

  if (visitante > local) {
    return `Gana ${awayTeamName || "Equipo visitante"}`;
  }

  return "Empate";
}

function PredictionForm({ match, onCancel }) {
  const [predictedHomeGoals, setPredictedHomeGoals] = useState(0);
  const [predictedAwayGoals, setPredictedAwayGoals] = useState(0);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const tendencia = calcularTendencia(
    predictedHomeGoals,
    predictedAwayGoals,
    match.homeTeamName,
    match.awayTeamName
  );

  function cambiarGoles(equipo, cambio) {
    setMensaje("");
    setError("");

    if (equipo === "home") {
      setPredictedHomeGoals((valor) => Math.max(0, valor + cambio));
      return;
    }

    setPredictedAwayGoals((valor) => Math.max(0, valor + cambio));
  }

  async function manejarSubmit(evento) {
    evento.preventDefault();

    setMensaje("");
    setError("");
    setGuardando(true);

    try {
      await guardarPronostico(
        match.id,
        predictedHomeGoals,
        predictedAwayGoals
      );

      setMensaje("Pronóstico guardado correctamente");
    } catch (error) {
      setError(error.message);
    } finally {
      setGuardando(false);
    }
  }

  return (
    <form className="prediction-form" onSubmit={manejarSubmit}>
      <div className="prediction-form__header">
        <h3>Tu pronóstico</h3>
        <p>Podés guardarlo de nuevo para actualizarlo.</p>
      </div>

      <div className="prediction-form__teams">
        <div className="prediction-counter">
          <span>{match.homeTeamName || "Equipo local"}</span>
          <div className="prediction-counter__controls">
            <Button
              disabled={guardando || predictedHomeGoals === 0}
              onClick={() => cambiarGoles("home", -1)}
              size="sm"
              type="button"
              variant="secondary"
            >
              -
            </Button>
            <strong>{predictedHomeGoals}</strong>
            <Button
              disabled={guardando}
              onClick={() => cambiarGoles("home", 1)}
              size="sm"
              type="button"
              variant="secondary"
            >
              +
            </Button>
          </div>
        </div>

        <div className="prediction-counter">
          <span>{match.awayTeamName || "Equipo visitante"}</span>
          <div className="prediction-counter__controls">
            <Button
              disabled={guardando || predictedAwayGoals === 0}
              onClick={() => cambiarGoles("away", -1)}
              size="sm"
              type="button"
              variant="secondary"
            >
              -
            </Button>
            <strong>{predictedAwayGoals}</strong>
            <Button
              disabled={guardando}
              onClick={() => cambiarGoles("away", 1)}
              size="sm"
              type="button"
              variant="secondary"
            >
              +
            </Button>
          </div>
        </div>
      </div>

      <div className="prediction-trend">
        <span>Tendencia</span>
        <strong>{tendencia}</strong>
      </div>

      <ErrorMessage message={error} />

      {mensaje && (
        <p className="prediction-success" role="status">
          {mensaje}
        </p>
      )}

      <div className="prediction-form__actions">
        <Button
          fullWidth
          isLoading={guardando}
          type="submit"
          variant="primary"
        >
          {guardando ? "Guardando..." : "Guardar pronóstico"}
        </Button>
        <Button
          disabled={guardando}
          fullWidth
          onClick={onCancel}
          type="button"
          variant="secondary"
        >
          Cancelar
        </Button>
      </div>
    </form>
  );
}

export default PredictionForm;
