function Spinner({
  size = 20,
  color = "currentColor",
  label = "Cargando",
  className = "",
  style,
  ...props
}) {
  const classes = ["ui-spinner", className].filter(Boolean).join(" ");

  return (
    <span
      aria-label={label}
      className={classes}
      role="status"
      style={{
        alignItems: "center",
        color,
        display: "inline-flex",
        height: size,
        justifyContent: "center",
        lineHeight: 0,
        width: size,
        ...style,
      }}
      {...props}
    >
      <svg aria-hidden="true" height={size} viewBox="0 0 24 24" width={size}>
        <circle
          cx="12"
          cy="12"
          fill="none"
          r="9"
          stroke="currentColor"
          strokeOpacity="0.25"
          strokeWidth="4"
        />
        <path
          d="M21 12a9 9 0 0 1-9 9"
          fill="none"
          stroke="currentColor"
          strokeLinecap="round"
          strokeWidth="4"
        >
          <animateTransform
            attributeName="transform"
            dur="0.8s"
            from="0 12 12"
            repeatCount="indefinite"
            to="360 12 12"
            type="rotate"
          />
        </path>
      </svg>
    </span>
  );
}

export default Spinner;
