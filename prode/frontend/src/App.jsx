import { useNavigate } from "react-router";
import Badge from "./components/ui/Badge.jsx";
import Button from "./components/ui/Button.jsx";
import Card from "./components/ui/Card.jsx";
import "./App.css";

const features = [
  {
    title: "Pronósticos en vivo",
    description: "Cargá resultados antes del inicio y seguí cada fecha.",
    accent: "primary",
  },
  {
    title: "Ranking global",
    description: "Sumá puntos, compará posiciones y peleá la cima.",
    accent: "success",
  },
  {
    title: "Grupos privados",
    description: "Armá torneos con amigos, compañeros o tu comisión.",
    accent: "amber",
  },
  {
    title: "Resultados automáticos",
    badge: "Cálculo de puntos",
    description:
      "Cuando se carga el resultado, el sistema calcula puntos y actualiza rankings al instante.",
    accent: "primary",
  },
];

function App() {
  const navigate = useNavigate();

  function irARegistro() {
    navigate("/registro");
  }

  function irALogin() {
    navigate("/login");
  }

  return (
    <main className="home-page">
      <section className="home-hero">
        <div className="home-hero__content">
          <Badge variant="amber">Temporada UTN</Badge>
          <h1>Prode UTN</h1>
          <p>
            Pronosticá partidos, competí con tus amigos y subí en el ranking.
          </p>

          <div className="home-actions">
            <Button onClick={irALogin} size="lg">
              Iniciar sesión
            </Button>
            <Button onClick={irARegistro} size="lg" variant="secondary">
              Crear cuenta
            </Button>
          </div>
        </div>

        <Card className="home-match-card" padding="lg">
          <div className="home-match-card__header">
            <div>
              <span className="home-eyebrow">Partido destacado</span>
              <h2>River Plate vs Boca Juniors</h2>
            </div>
            <Badge variant="amber">POR_JUGARSE</Badge>
          </div>

          <div className="home-pitch" aria-hidden="true">
            <span className="home-pitch__line" />
            <span className="home-pitch__circle" />
          </div>

          <div className="home-score-preview">
            <div>
              <span>River Plate</span>
              <strong>2</strong>
            </div>
            <span className="home-score-preview__separator">-</span>
            <div>
              <strong>1</strong>
              <span>Boca Juniors</span>
            </div>
          </div>

          <p className="home-prediction">Pronóstico ejemplo: 2 - 1</p>
        </Card>
      </section>

      <section className="home-features" aria-label="Funciones principales">
        {features.map((feature) => (
          <Card className="home-feature-card" key={feature.title}>
            <Badge size="sm" variant={feature.accent}>
              {feature.badge || feature.title}
            </Badge>
            <h2>{feature.title}</h2>
            <p>{feature.description}</p>
          </Card>
        ))}
      </section>
    </main>
  );
}

export default App;
