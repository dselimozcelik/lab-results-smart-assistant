import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { getLabResults } from "../api/labResults";
import type { LabResultFilters } from "../api/labResults";
import { StatusBadge } from "../components/StatusBadge";
import { FilterBar } from "../components/FilterBar";
import "./ResultsPage.css";

// datetime-local gives "2026-06-07T10:30"; the backend wants a full ISO instant.
function toApiFilters(f: LabResultFilters): LabResultFilters {
  return {
    ...f,
    from: f.from ? new Date(f.from).toISOString() : undefined,
    to: f.to ? new Date(f.to).toISOString() : undefined,
  };
}

export function ResultsPage() {
  const [filters, setFilters] = useState<LabResultFilters>({});
  const navigate = useNavigate();

  const { data, isPending, isError, error } = useQuery({
    queryKey: ["labResults", filters],
    queryFn: () => getLabResults(toApiFilters(filters)),
    placeholderData: keepPreviousData,
  });

  return (
    <div className="results-page">
      <div className="results-header">
        <h1 className="results-title">Lab Sonuçları</h1>
        <span className="results-count">{data ? `${data.totalElements} sonuç` : ""}</span>
      </div>

      <FilterBar value={filters} onChange={setFilters} />

      {isError && (
        <p className="state-message" role="alert">
          Sonuçlar yüklenemedi: {(error as Error).message}
        </p>
      )}

      {isPending && <p className="state-message">Yükleniyor…</p>}

      {data && (
      <div className="results-card">
        <table className="results-table">
          <thead>
            <tr>
              <th>Hasta</th>
              <th>Test</th>
              <th>Değer</th>
              <th>Referans</th>
              <th>Durum</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((r) => (
              <tr
                key={r.id}
                onClick={() => navigate(`/results/${r.id}`)}
                className={`row-clickable ${r.anomalyStatus === "CRITICAL" ? "row-critical" : ""}`}
              >
                <td className="cell-muted">{r.patientId}</td>
                <td>{r.testName}</td>
                <td className="cell-value">
                  {r.value ?? "—"} <span className="cell-muted">{r.unit ?? ""}</span>
                </td>
                <td className="cell-value cell-muted">
                  {r.referenceMin ?? "—"} – {r.referenceMax ?? "—"}
                </td>
                <td>
                  <StatusBadge status={r.anomalyStatus} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      )}

      {data && data.content.length === 0 && (
        <p className="state-message">Bu filtrelere uyan sonuç yok.</p>
      )}
    </div>
  );
}
