import { apiFetch } from "./client";

export type AnomalyStatus = "NORMAL" | "LOW" | "HIGH" | "CRITICAL" | "INVALID";

export type LabResult = {
  id: number;
  sampleId: string;
  patientId: string;
  testCode: string;
  testName: string;
  value: number | null;
  unit: string | null;
  referenceMin: number | null;
  referenceMax: number | null;
  measuredAt: string;
  deviceId: string;
  anomalyStatus: AnomalyStatus;
  createdAt: string;
};

// Mirrors Spring Data's Page<T> JSON shape (only the fields we use).
export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type LabResultFilters = {
  patientId?: string;
  testCode?: string;
  status?: AnomalyStatus | "";
  from?: string;
  to?: string;
};

export function getLabResult(id: number | string): Promise<LabResult> {
  return apiFetch<LabResult>(`/api/lab-results/${id}`);
}

export function getLabResults(filters: LabResultFilters = {}): Promise<Page<LabResult>> {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(filters)) {
    if (value) {
      params.set(key, value);
    }
  }
  const query = params.toString();
  return apiFetch<Page<LabResult>>(`/api/lab-results${query ? `?${query}` : ""}`);
}
