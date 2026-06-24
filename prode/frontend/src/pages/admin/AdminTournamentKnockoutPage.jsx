import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import KnockoutRoundColumn from "../../components/knockout/KnockoutRoundColumn.jsx";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import ErrorMessage from "../../components/ui/ErrorMessage.jsx";
import Spinner from "../../components/ui/Spinner.jsx";
import {
  advanceKnockout,
  generateKnockout,
  getKnockout,
} from "../../services/knockoutService.js";
import { guardarResultado } from "../../services/tournamentResultService.js";
import { obtenerTorneoPorId } from "../../services/tournamentService.js";
import "../../App.css";

const formatoLabel = {
  GROUPS: "Por grupos",
  LEAGUE: "Tabla general",
};

const estadoVariant = {
  DRAFT: "neutral",
  ACTIVE: "success",
  FINISHED: "primary",
  ARCHIVED: "amber",
};

function leerSesion() {
  return {
    token: localStorage.getItem("token"),
    role: localStorage.getItem("role"),
  };
}

function tienePartidos(rounds) {
  return rounds.some(
    (round) => Array.isArray(round.matches) && round.matches.length > 0
  );
}

function mapearResultados(rounds) {
  return rounds.reduce((acc, round) => {
    const matches = Array.isArray(round.matches)
      ? round.matches
      : [];

    for (const match of matches) {
      acc[match.matchId] = {
        homeGoals:
          match.homeGoals === null ||
            match.homeGoals === undefined
            ? ""
            : String(match.homeGoals),

        awayGoals:
          match.awayGoals === null ||
            match.awayGoals === undefined
            ? ""
            : String(match.awayGoals),

        homePenaltyGoals:
          match.homePenaltyGoals === null ||
            match.homePenaltyGoals === undefined
            ? ""
            : String(match.homePenaltyGoals),

        awayPenaltyGoals:
          match.awayPenaltyGoals === null ||
            match.awayPenaltyGoals === undefined
            ? ""
            : String(match.awayPenaltyGoals),
      };
    }

    return acc;
  }, {});
}

function convertirFechaLocalAIso(valor) {
  if (!valor) {
    throw new Error("El horario es obligatorio.");
  }

  const fecha = new Date(valor);

  if (Number.isNaN(fecha.getTime())) {
    throw new Error("El horario no es valido.");
  }

  return fecha.toISOString();
}

function validarGoles(resultado) {
  const homeGoals = Number(resultado?.homeGoals);
  const awayGoals = Number(resultado?.awayGoals);

  if (
    resultado?.homeGoals === "" ||
    resultado?.awayGoals === ""
  ) {
    throw new Error(
      "Los dos campos de goles son obligatorios."
    );
  }

  if (
    !Number.isInteger(homeGoals) ||
    !Number.isInteger(awayGoals) ||
    homeGoals < 0 ||
    awayGoals < 0
  ) {
    throw new Error(
      "Los goles deben ser enteros mayores o iguales a 0."
    );
  }

  let homePenaltyGoals = null;
  let awayPenaltyGoals = null;

  const partidoEmpatado = homeGoals === awayGoals;

  if (partidoEmpatado) {
    if (
      resultado?.homePenaltyGoals === "" ||
      resultado?.awayPenaltyGoals === "" ||
      resultado?.homePenaltyGoals === undefined ||
      resultado?.awayPenaltyGoals === undefined
    ) {
      throw new Error(
        "Si el partido termina empatado, se deben cargar los penales."
      );
    }

    homePenaltyGoals = Number(
      resultado.homePenaltyGoals
    );

    awayPenaltyGoals = Number(
      resultado.awayPenaltyGoals
    );

    if (
      !Number.isInteger(homePenaltyGoals) ||
      !Number.isInteger(awayPenaltyGoals) ||
      homePenaltyGoals < 0 ||
      awayPenaltyGoals < 0
    ) {
      throw new Error(
        "Los goles por penales deben ser números enteros mayores o iguales a 0."
      );
    }

    if (homePenaltyGoals === awayPenaltyGoals) {
      throw new Error(
        "La definición por penales no puede terminar empatada."
      );
    }
  }

  return {
    homeGoals,
    awayGoals,
    homePenaltyGoals,
    awayPenaltyGoals,
  };
}

function AdminTournamentKnockoutPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [sesion] = useState(leerSesion);
  const [torneo, setTorneo] = useState(null);
  const [bracket, setBracket] = useState(null);
  const [resultados, setResultados] = useState({});
  const [generateForm, setGenerateForm] = useState({
    qualifiersCount: "4",
    qualifiedPerGroup: "2",
    firstRoundStartTime: "",
  });
  const [advanceForm, setAdvanceForm] = useState({
    nextRoundStartTime: "",
  });
  const [cargando, setCargando] = useState(true);
  const [accion, setAccion] = useState("");
  const [savingMatchId, setSavingMatchId] = useState(null);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const rounds = Array.isArray(bracket?.rounds) ? bracket.rounds : [];
  const hasMatches = tienePartidos(rounds);

  useEffect(() => {
    if (!sesion.token) {
      navigate("/login", { replace: true });
      return;
    }

    if (sesion.role !== "ADMIN") {
      navigate("/dashboard", { replace: true });
      return;
    }

    let activo = true;

    async function cargarDatos() {
      setCargando(true);
      setError("");

      try {
        const [torneoData, bracketData] = await Promise.all([
          obtenerTorneoPorId(tournamentId),
          getKnockout(tournamentId),
        ]);

        if (activo) {
          setTorneo(torneoData);
          setBracket(bracketData);
          setResultados(mapearResultados(bracketData?.rounds || []));
        }
      } catch (error) {
        if (activo) {
          setError(error.message);
        }
      } finally {
        if (activo) {
          setCargando(false);
        }
      }
    }

    cargarDatos();

    return () => {
      activo = false;
    };
  }, [navigate, sesion.role, sesion.token, tournamentId]);

  async function recargarLlave() {
    const data = await getKnockout(tournamentId);
    setBracket(data);
    setResultados(mapearResultados(data?.rounds || []));
    return data;
  }

  function actualizarGenerate(campo, valor) {
    setMensaje("");
    setError("");
    setGenerateForm((actual) => ({ ...actual, [campo]: valor }));
  }

  function actualizarAdvance(campo, valor) {
    setMensaje("");
    setError("");
    setAdvanceForm((actual) => ({ ...actual, [campo]: valor }));
  }

  function actualizarResultado(matchId, campo, valor) {
    setMensaje("");
    setError("");
    setResultados((actual) => ({
      ...actual,
      [matchId]: {
        ...(actual[matchId] || {}),
        [campo]: valor,
      },
    }));
  }

  async function ejecutarGeneracion(evento) {
    evento.preventDefault();
    setAccion("generate");
    setMensaje("");
    setError("");

    try {
      const payload = {
        qualifiersCount: Number(generateForm.qualifiersCount),
        qualifiedPerGroup:
          generateForm.qualifiedPerGroup === ""
            ? null
            : Number(generateForm.qualifiedPerGroup),
        firstRoundStartTime: convertirFechaLocalAIso(
          generateForm.firstRoundStartTime
        ),
      };
      const data = await generateKnockout(tournamentId, payload);
      setBracket(data);
      setResultados(mapearResultados(data?.rounds || []));
      setMensaje("Llave eliminatoria generada correctamente.");
    } catch (error) {
      setError(error.message);
    } finally {
      setAccion("");
    }
  }

  async function ejecutarAvance(evento) {
    evento.preventDefault();
    setAccion("advance");
    setMensaje("");
    setError("");

    try {
      const data = await advanceKnockout(tournamentId, {
        nextRoundStartTime: convertirFechaLocalAIso(
          advanceForm.nextRoundStartTime
        ),
      });
      setBracket(data);
      setResultados(mapearResultados(data?.rounds || []));
      setMensaje("Ronda avanzada correctamente.");
    } catch (error) {
      setError(error.message);
    } finally {
      setAccion("");
    }
  }

  async function ejecutarGuardadoResultado(match) {
    setSavingMatchId(match.matchId);
    setMensaje("");
    setError("");

    try {
      const {
        homeGoals,
        awayGoals,
        homePenaltyGoals,
        awayPenaltyGoals,
      } = validarGoles(resultados[match.matchId]);

      await guardarResultado(
        tournamentId,
        match.matchId,
        homeGoals,
        awayGoals,
        homePenaltyGoals,
        awayPenaltyGoals
      );
      await recargarLlave();
      setMensaje("Resultado eliminatorio guardado correctamente.");
    } catch (error) {
      setError(error.message);
    } finally {
      setSavingMatchId(null);
    }
  }

  if (!sesion.token || sesion.role !== "ADMIN") {
    return null;
  }

  return (
    <main className="admin-page admin-knockout-page">
      <section className="admin-shell">
        <header className="admin-header">
          <div>
            <Badge variant="amber">Eliminatorias</Badge>
            <h1>{torneo?.name || "Llave eliminatoria"}</h1>
            <p>Genera la llave, carga resultados y avanza rondas.</p>
          </div>

          <div className="admin-header__actions">
            <Button
              onClick={() => navigate(`/admin/tournaments/${tournamentId}`)}
              variant="secondary"
            >
              Volver al torneo
            </Button>
          </div>
        </header>

        {cargando && (
          <div className="admin-loading" role="status">
            <Spinner size={28} />
            <span>Cargando llave eliminatoria...</span>
          </div>
        )}

        {!cargando && torneo && (
          <>
            <Card className="admin-tournament-detail-card">
              <Badge size="sm" variant={estadoVariant[torneo.status] || "neutral"}>
                {torneo.status}
              </Badge>
              <Badge size="sm" variant="primary">
                {formatoLabel[torneo.format || "LEAGUE"]}
              </Badge>
              <h2>Administracion de eliminatorias</h2>
              <p>
                La llave usa los clasificados de la tabla deportiva. Los ganadores
                son definidos por el backend al guardar resultados.
              </p>
            </Card>

            {!hasMatches && (
              <Card className="admin-knockout-action-card">
                <div>
                  <Badge size="sm" variant="success">
                    Primera ronda
                  </Badge>
                  <h2>Generar llave eliminatoria</h2>
                  <p>
                    Selecciona clasificados y horario inicial para crear la primera
                    ronda desde standings.
                  </p>
                </div>

                <form className="admin-knockout-form" onSubmit={ejecutarGeneracion}>
                  <div className="admin-field">
                    <label htmlFor="qualifiersCount">Clasificados</label>
                    <select
                      id="qualifiersCount"
                      onChange={(evento) =>
                        actualizarGenerate("qualifiersCount", evento.target.value)
                      }
                      value={generateForm.qualifiersCount}
                    >
                      <option value="4">4 equipos</option>
                      <option value="8">8 equipos</option>
                      <option value="16">16 equipos</option>
                    </select>
                  </div>

                  <div className="admin-field">
                    <label htmlFor="qualifiedPerGroup">Clasificados por grupo</label>
                    <input
                      id="qualifiedPerGroup"
                      min={1}
                      onChange={(evento) =>
                        actualizarGenerate("qualifiedPerGroup", evento.target.value)
                      }
                      type="number"
                      value={generateForm.qualifiedPerGroup}
                    />
                  </div>

                  <div className="admin-field">
                    <label htmlFor="firstRoundStartTime">Inicio primera ronda</label>
                    <input
                      id="firstRoundStartTime"
                      onChange={(evento) =>
                        actualizarGenerate(
                          "firstRoundStartTime",
                          evento.target.value
                        )
                      }
                      type="datetime-local"
                      value={generateForm.firstRoundStartTime}
                    />
                  </div>

                  <Button
                    fullWidth
                    isLoading={accion === "generate"}
                    type="submit"
                    variant="primary"
                  >
                    Generar llave
                  </Button>
                </form>
              </Card>
            )}

            {hasMatches && (
              <Card className="admin-knockout-action-card">
                <div>
                  <Badge size="sm" variant="amber">
                    Siguiente ronda
                  </Badge>
                  <h2>Avanzar llave</h2>
                  <p>
                    Cuando todos los partidos de la ronda actual esten finalizados,
                    genera la siguiente ronda con sus ganadores.
                  </p>
                </div>

                <form className="admin-knockout-form" onSubmit={ejecutarAvance}>
                  <div className="admin-field">
                    <label htmlFor="nextRoundStartTime">Inicio siguiente ronda</label>
                    <input
                      id="nextRoundStartTime"
                      onChange={(evento) =>
                        actualizarAdvance("nextRoundStartTime", evento.target.value)
                      }
                      type="datetime-local"
                      value={advanceForm.nextRoundStartTime}
                    />
                  </div>

                  <Button
                    fullWidth
                    isLoading={accion === "advance"}
                    type="submit"
                    variant="amber"
                  >
                    Avanzar ronda
                  </Button>
                </form>
              </Card>
            )}

            {mensaje && (
              <p className="admin-success" role="status">
                {mensaje}
              </p>
            )}
          </>
        )}

        {!cargando && <ErrorMessage message={error} />}

        {!cargando && !error && !hasMatches && (
          <EmptyState
            description="La administracion puede generar la primera ronda desde la tabla deportiva."
            title="Sin llave generada"
          />
        )}

        {!cargando && hasMatches && (
          <section className="knockout-bracket admin-knockout-bracket">
            {rounds.map((round) => (
              <KnockoutRoundColumn
                editable
                key={round.round}
                onResultChange={actualizarResultado}
                onSaveResult={ejecutarGuardadoResultado}
                resultValues={resultados}
                round={round}
                savingMatchId={savingMatchId}
              />
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default AdminTournamentKnockoutPage;
