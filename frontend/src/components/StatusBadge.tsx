import type { AnomalyStatus } from "../api/labResults";
import "./StatusBadge.css";

const LABELS: Record<AnomalyStatus, string> = {
  NORMAL: "Normal",
  LOW: "Düşük",
  HIGH: "Yüksek",
  CRITICAL: "Kritik",
  INVALID: "Geçersiz",
};

export function StatusBadge({ status }: { status: AnomalyStatus }) {
  return (
    <span className={`badge badge--${status.toLowerCase()}`}>
      {LABELS[status]}
    </span>
  );
}
