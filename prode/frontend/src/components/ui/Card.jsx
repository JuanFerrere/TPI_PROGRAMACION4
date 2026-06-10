const paddingValues = {
  none: 0,
  sm: 16,
  md: 24,
  lg: 32,
};

function Card({
  as: Component = "section",
  children,
  className = "",
  padding = "md",
  style,
  ...props
}) {
  const classes = ["ui-card", className].filter(Boolean).join(" ");

  return (
    <Component
      className={classes}
      style={{
        background: "var(--color-card)",
        border: "1px solid var(--color-border)",
        borderRadius: 8,
        color: "var(--color-text)",
        padding: paddingValues[padding] ?? paddingValues.md,
        ...style,
      }}
      {...props}
    >
      {children}
    </Component>
  );
}

export default Card;
