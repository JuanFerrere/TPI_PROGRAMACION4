import Spinner from "./Spinner.jsx";

const variants = {
  primary: {
    background: "var(--color-primary)",
    borderColor: "var(--color-primary)",
    color: "#FFFFFF",
  },
  secondary: {
    background: "var(--color-card)",
    borderColor: "var(--color-border)",
    color: "var(--color-text)",
  },
  amber: {
    background: "var(--color-amber)",
    borderColor: "var(--color-amber)",
    color: "#111827",
  },
  success: {
    background: "var(--color-success)",
    borderColor: "var(--color-success)",
    color: "#04130E",
  },
  danger: {
    background: "var(--color-danger)",
    borderColor: "var(--color-danger)",
    color: "#FFFFFF",
  },
  ghost: {
    background: "transparent",
    borderColor: "transparent",
    color: "var(--color-text)",
  },
};

const sizes = {
  sm: {
    fontSize: 14,
    minHeight: 34,
    padding: "7px 12px",
  },
  md: {
    fontSize: 15,
    minHeight: 40,
    padding: "9px 16px",
  },
  lg: {
    fontSize: 16,
    minHeight: 46,
    padding: "11px 20px",
  },
};

function Button({
  children,
  className = "",
  disabled = false,
  fullWidth = false,
  isLoading = false,
  size = "md",
  style,
  type = "button",
  variant = "primary",
  ...props
}) {
  const isDisabled = disabled || isLoading;
  const classes = ["ui-button", `ui-button--${variant}`, className]
    .filter(Boolean)
    .join(" ");

  return (
    <button
      aria-busy={isLoading || undefined}
      className={classes}
      disabled={isDisabled}
      style={{
        alignItems: "center",
        border: "1px solid",
        borderRadius: 8,
        cursor: isDisabled ? "not-allowed" : "pointer",
        display: "inline-flex",
        fontWeight: 700,
        gap: 8,
        justifyContent: "center",
        opacity: isDisabled ? 0.65 : 1,
        transition: "border-color 160ms ease, filter 160ms ease",
        width: fullWidth ? "100%" : undefined,
        ...(variants[variant] || variants.primary),
        ...(sizes[size] || sizes.md),
        ...style,
      }}
      type={type}
      {...props}
    >
      {isLoading && <Spinner color="currentColor" size={16} />}
      <span>{children}</span>
    </button>
  );
}

export default Button;
