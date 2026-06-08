import { AlertTriangle, ArrowDown, ArrowUp, Ban, Check, type LucideIcon } from "lucide-react";
import type { AnomalyStatus } from "../api/labResults";
import { STATUS_LABELS } from "../utils/labUtils";
import "./StatusBadge.css";

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
      {STATUS_LABELS[status]}
    </span>
  );
}
