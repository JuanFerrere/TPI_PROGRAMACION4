import Card from "./Card.jsx";

function EmptyState({
  action,
  className = "",
  description,
  icon,
  style,
  title = "Sin resultados",
  ...props
}) {
  const classes = ["ui-empty-state", className].filter(Boolean).join(" ");

  return (
    <Card
      className={classes}
      style={{
        alignItems: "center",
        display: "flex",
        flexDirection: "column",
        gap: 12,
        justifyContent: "center",
        minHeight: 180,
        textAlign: "center",
        ...style,
      }}
      {...props}
    >
      {icon && (
        <div
          aria-hidden="true"
          style={{
            color: "var(--color-amber)",
            display: "inline-flex",
            fontSize: 32,
            lineHeight: 1,
          }}
        >
          {icon}
        </div>
      )}

      <div>
        <h2
          style={{
            color: "var(--color-text)",
            fontSize: 20,
            lineHeight: 1.2,
            margin: 0,
          }}
        >
          {title}
        </h2>

        {description && (
          <p
            style={{
              color: "var(--color-text-muted)",
              lineHeight: 1.5,
              margin: "8px 0 0",
            }}
          >
            {description}
          </p>
        )}
      </div>

      {action && <div style={{ marginTop: 4 }}>{action}</div>}
    </Card>
  );
}

export default EmptyState;
