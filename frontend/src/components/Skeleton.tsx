import "./Skeleton.css";

type SkeletonProps = {
  width?: string;
  height?: string;
  radius?: string;
  className?: string;
};

// A single shimmering placeholder block. Compose several to mirror real content layout.
export function Skeleton({ width, height, radius, className }: SkeletonProps) {
  return (
    <span
      className={`skeleton${className ? ` ${className}` : ""}`}
      style={{ width, height, borderRadius: radius }}
      aria-hidden="true"
    />
  );
}

// Loading stand-in for a few patient/table rows, announced politely to screen readers.
export function SkeletonRows({ rows = 6 }: { rows?: number }) {
  return (
    <div className="skeleton-rows" role="status" aria-label="Yükleniyor">
      {Array.from({ length: rows }).map((_, i) => (
        <div className="skeleton-row" key={i}>
          <Skeleton width="22%" height="14px" />
          <Skeleton width="14%" height="14px" />
          <Skeleton width="18%" height="14px" />
          <Skeleton width="20%" height="14px" />
        </div>
      ))}
    </div>
  );
}
