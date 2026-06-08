import { AlertTriangle, ArrowDown, ArrowUp, Ban, Check, type LucideIcon } from "lucide-react";
import type { AnomalyStatus } from "../api/labResults";
import "./StatusBadge.css";

const LABELS: Record<AnomalyStatus, string> = {
  NORMAL: "Normal",
  LOW: "Düşük",
  HIGH: "Yüksek",
  CRITICAL: "Kritik",
  INVALID: "Geçersiz",
};

// A redundant glyph per status so the meaning survives colour-blindness and grayscale printing.
const ICONS: Record<AnomalyStatus, LucideIcon> = {
  NORMAL: Check,
  LOW: ArrowDown,
  HIGH: ArrowUp,
  CRITICAL: AlertTriangle,
  INVALID: Ban,
};

export function StatusBadge({ status }: { status: AnomalyStatus }) {
  const Icon = ICONS[status];
  return (
    <span className={`badge badge--${status.toLowerCase()}`}>
      <Icon className="badge-icon" size={13} strokeWidth={2.5} aria-hidden="true" />
      {LABELS[status]}
    </span>
  );
}
