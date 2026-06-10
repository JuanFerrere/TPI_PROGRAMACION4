function ErrorMessage({
  children,
  className = "",
  message,
  role = "alert",
  style,
  ...props
}) {
  const content = children ?? message;

  if (!content) {
    return null;
  }

  const classes = ["ui-error-message", className].filter(Boolean).join(" ");

  return (
    <p
      className={classes}
      role={role}
      style={{
        background: "rgba(239, 68, 68, 0.12)",
        border: "1px solid rgba(239, 68, 68, 0.35)",
        borderRadius: 8,
        color: "var(--color-danger)",
        margin: 0,
        padding: "10px 12px",
        ...style,
      }}
      {...props}
    >
      {content}
    </p>
  );
}

export default ErrorMessage;
