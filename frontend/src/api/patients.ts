import { apiFetch } from "./client";
import type { AnomalyStatus, LabResult, Page } from "./labResults";

// Level-1 row: one patient with an at-a-glance rollup across all their tubes.
export type PatientSummary = {
  patientId: string;
  testCount: number;
  sampleCount: number;
  worstStatus: AnomalyStatus;
  lastMeasuredAt: string;
};

// Level-2: one tube (sample) and the panel of tests it carries.
export type SampleGroup = {
  sampleId: string;
  measuredAt: string;
  deviceId: string;
  worstStatus: AnomalyStatus;
  tests: LabResult[];
};

export type PatientDetail = {
  patientId: string;
  samples: SampleGroup[];
};

export type PatientQuery = {
  patientId?: string;
  page?: number;
  size?: number;
  sort?: string;
};

export function getPatients(query: PatientQuery = {}): Promise<Page<PatientSummary>> {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== "") {
      params.set(key, String(value));
    }
  }
  const qs = params.toString();
  return apiFetch<Page<PatientSummary>>(`/api/patients${qs ? `?${qs}` : ""}`);
}

export function getPatientSuggestions(query: string): Promise<string[]> {
  const params = new URLSearchParams({ query, limit: "8" });
  return apiFetch<string[]>(`/api/patients/suggestions?${params}`);
}

export function getPatient(patientId: string): Promise<PatientDetail> {
  return apiFetch<PatientDetail>(`/api/patients/${encodeURIComponent(patientId)}`);
}
