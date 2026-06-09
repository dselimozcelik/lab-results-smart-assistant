import { keepPreviousData, useQuery } from "@tanstack/react-query";
import {
  getPatient,
  getPatients,
  getPatientSuggestions,
  type PatientQuery,
} from "../api/patients";
import { REFETCH_MS } from "../config";

// Query keys in one place so cache reads/invalidations stay consistent across the app.
export const patientKeys = {
  list: (query: PatientQuery) => ["patients", query] as const,
  detail: (patientId: string) => ["patient", patientId] as const,
  suggestions: (query: string) => ["patientSuggestions", query] as const,
};

// Patient list, refetched on the same cadence as backend polling, keeping the previous page
// visible during fetches.
export function usePatients(query: PatientQuery) {
  return useQuery({
    queryKey: patientKeys.list(query),
    queryFn: () => getPatients(query),
    placeholderData: keepPreviousData,
    refetchInterval: REFETCH_MS,
  });
}

// One patient's tubes and panels.
export function usePatient(patientId: string | undefined) {
  return useQuery({
    queryKey: patientKeys.detail(patientId ?? ""),
    queryFn: () => getPatient(patientId!),
    enabled: Boolean(patientId),
  });
}

// Autocomplete suggestions; only runs when enabled and the query is long enough.
export function usePatientSuggestions(query: string, enabled: boolean) {
  return useQuery({
    queryKey: patientKeys.suggestions(query.toLocaleLowerCase("tr-TR")),
    queryFn: () => getPatientSuggestions(query),
    enabled: enabled && query.length >= 2,
    staleTime: 60_000,
  });
}
