const variants = {
  primary: {
    background: "rgba(59, 130, 246, 0.14)",
    borderColor: "rgba(59, 130, 246, 0.38)",
    color: "var(--color-primary)",
  },
  amber: {
    background: "rgba(245, 158, 11, 0.14)",
    borderColor: "rgba(245, 158, 11, 0.38)",
    color: "var(--color-amber)",
  },
  success: {
    background: "rgba(16, 185, 129, 0.14)",
    borderColor: "rgba(16, 185, 129, 0.38)",
    color: "var(--color-success)",
  },
  danger: {
    background: "rgba(239, 68, 68, 0.14)",
    borderColor: "rgba(239, 68, 68, 0.38)",
    color: "var(--color-danger)",
  },
  neutral: {
    background: "rgba(100, 116, 139, 0.14)",
    borderColor: "rgba(100, 116, 139, 0.38)",
    color: "var(--color-text-muted)",
  },
};

const sizes = {
  sm: {
    fontSize: 12,
    padding: "3px 8px",
  },
  md: {
    fontSize: 13,
    padding: "4px 10px",
  },
};

function Badge({
  children,
  className = "",
  size = "md",
  style,
  variant = "neutral",
  ...props
}) {
  const classes = ["ui-badge", `ui-badge--${variant}`, className]
    .filter(Boolean)
    .join(" ");

  return (
    <span
      className={classes}
      style={{
        alignItems: "center",
        border: "1px solid",
        borderRadius: 999,
        display: "inline-flex",
        fontWeight: 700,
        lineHeight: 1,
        whiteSpace: "nowrap",
        ...(variants[variant] || variants.neutral),
        ...(sizes[size] || sizes.md),
        ...style,
      }}
      {...props}
    >
      {children}
    </span>
  );
}

export default Badge;
