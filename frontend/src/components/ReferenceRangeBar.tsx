import type { AnomalyStatus } from "../api/labResults";
import "./ReferenceRangeBar.css";

type Props = {
  value: number | null;
  referenceMin: number | null;
  referenceMax: number | null;
  status: AnomalyStatus;
};

// A horizontal track showing the reference band [min..max] with a marker at the measured value.
// The band sits in the middle 60% of the track so out-of-range values still have room to show.
// Degrades gracefully when value or bounds are missing (e.g. an INVALID test).
export function ReferenceRangeBar({ value, referenceMin, referenceMax, status }: Props) {
  if (value == null || referenceMin == null || referenceMax == null || referenceMax <= referenceMin) {
    return <div className="refbar refbar--empty" aria-hidden="true">—</div>;
  }

  const span = referenceMax - referenceMin;
  // Map the reference band to 20%..80% of the track; clamp the marker to 0..100%.
  const rawPct = 20 + ((value - referenceMin) / span) * 60;
  const markerPct = Math.max(2, Math.min(98, rawPct));

  return (
    <div className="refbar" role="img"
         aria-label={`Değer ${value}, referans ${referenceMin}–${referenceMax}`}>
      <div className="refbar-track">
        <div className="refbar-band" />
        <div className={`refbar-marker refbar-marker--${status.toLowerCase()}`}
             style={{ left: `${markerPct}%` }} />
      </div>
      {/* Labels sit under the band edges (20% / 80%), not the track ends, so they line up with the green range. */}
      <div className="refbar-bounds">
        <span className="refbar-bound" style={{ left: "20%" }}>{referenceMin}</span>
        <span className="refbar-bound" style={{ left: "80%" }}>{referenceMax}</span>
      </div>
    </div>
  );
}
