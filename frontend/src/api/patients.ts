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
  testCode?: string;
  status?: AnomalyStatus;
  from?: string;
  to?: string;
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

// One test's numeric history for a patient, oldest to newest, for the trend sparkline.
export type TestHistoryPoint = {
  measuredAt: string;
  value: number;
};

export function getTestHistory(patientId: string, testCode: string): Promise<TestHistoryPoint[]> {
  return apiFetch<TestHistoryPoint[]>(
    `/api/patients/${encodeURIComponent(patientId)}/tests/${encodeURIComponent(testCode)}/history`,
  );
}
