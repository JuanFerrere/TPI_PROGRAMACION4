import Badge from "../ui/Badge.jsx";
import KnockoutMatchCard from "./KnockoutMatchCard.jsx";

function KnockoutRoundColumn({
  editable = false,
  onResultChange,
  onSaveResult,
  resultValues = {},
  round,
  savingMatchId,
}) {
  const matches = Array.isArray(round.matches) ? round.matches : [];

  return (
    <section className="knockout-round-column">
      <div className="knockout-round-column__header">
        <Badge size="sm" variant="primary">
          {round.round}
        </Badge>
        <h2>{round.label || round.round}</h2>
        <span>{matches.length} partidos</span>
      </div>

      <div className="knockout-round-column__matches">
        {matches.map((match) => (
          <KnockoutMatchCard
            editable={editable}
            isSaving={savingMatchId === match.matchId}
            key={match.matchId}
            match={match}
            onResultChange={onResultChange}
            onSaveResult={onSaveResult}
            resultValue={resultValues[match.matchId]}
          />
        ))}
      </div>
    </section>
  );
}

export default KnockoutRoundColumn;
