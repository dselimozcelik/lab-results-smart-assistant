import type { AnomalyStatus, LabResult } from "../api/labResults";

// Turkish labels for each anomaly status, shared by the badge, filters and the active-filter chips.
export const STATUS_LABELS: Record<AnomalyStatus, string> = {
  NORMAL: "Normal",
  LOW: "Düşük",
  HIGH: "Yüksek",
  CRITICAL: "Kritik",
  INVALID: "Geçersiz",
};

// Ordering weight for "most severe first". HIGH and LOW share a rank: both are out of range, and
// neither is inherently worse than the other without clinical context.
const SEVERITY: Record<AnomalyStatus, number> = {
  CRITICAL: 4,
  HIGH: 3,
  LOW: 3,
  INVALID: 2,
  NORMAL: 1,
};

export function isAbnormal(status: AnomalyStatus): boolean {
  return status !== "NORMAL";
}

// A copy of the tests sorted so a doctor sees problems at the top of each tube.
export function abnormalFirst(tests: LabResult[]): LabResult[] {
  return [...tests].sort((a, b) => SEVERITY[b.anomalyStatus] - SEVERITY[a.anomalyStatus]);
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("tr-TR");
}
