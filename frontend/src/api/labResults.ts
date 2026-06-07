// Shared types for lab data. The patient-centric API (api/patients.ts) reuses these.

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
