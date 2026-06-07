import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { getPatients } from "../api/patients";
import type { PatientQuery } from "../api/patients";
import { PatientFilters } from "../components/PatientFilters";
import type { PatientFilterValues } from "../components/PatientFilters";
import { PatientSearchBar } from "../components/PatientSearchBar";
import { StatusBadge } from "../components/StatusBadge";
import "./ResultsPage.css";

const DEFAULT_PAGE_SIZE = 20;
const EMPTY_FILTERS: PatientFilterValues = { testCode: "", status: "", from: "", to: "" };
const STATUS_LABELS = {
  NORMAL: "Normal",
  LOW: "Düşük",
  HIGH: "Yüksek",
  CRITICAL: "Kritik",
  INVALID: "Geçersiz",
} as const;

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString("tr-TR");
}

function startOfDay(date: string): string | undefined {
  return date ? new Date(`${date}T00:00:00`).toISOString() : undefined;
}

function endOfDay(date: string): string | undefined {
  return date ? new Date(`${date}T23:59:59.999`).toISOString() : undefined;
}

export function PatientsPage() {
  const [draftPatientId, setDraftPatientId] = useState("");
  const [patientId, setPatientId] = useState("");
  const [draftFilters, setDraftFilters] = useState<PatientFilterValues>(EMPTY_FILTERS);
  const [filters, setFilters] = useState<PatientFilterValues>(EMPTY_FILTERS);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const navigate = useNavigate();

  const query: PatientQuery = {
    patientId: patientId || undefined,
    testCode: filters.testCode.trim() || undefined,
    status: filters.status || undefined,
    from: startOfDay(filters.from),
    to: endOfDay(filters.to),
    page,
    size: pageSize,
  };

  const { data, isPending, isFetching, isError, error } = useQuery({
    queryKey: ["patients", query],
    queryFn: () => getPatients(query),
    placeholderData: keepPreviousData,
    refetchInterval: 10_000,
  });

  function changePageSize(next: number) {
    setPageSize(next);
    setPage(0);
  }

  function applyAllFilters() {
    setPatientId(draftPatientId.trim());
    setFilters({ ...draftFilters, testCode: draftFilters.testCode.trim() });
    setPage(0);
  }

  function clearPatientSearch() {
    setDraftPatientId("");
    setPatientId("");
    setPage(0);
  }

  function clearAdvancedFilters() {
    setDraftFilters(EMPTY_FILTERS);
    setFilters(EMPTY_FILTERS);
    setPage(0);
  }

  function clearAllFilters() {
    setDraftPatientId("");
    setPatientId("");
    clearAdvancedFilters();
  }

  const activeAdvancedFilterCount = Object.values(filters).filter(Boolean).length;
  const hasAnyFilter = Boolean(patientId) || activeAdvancedFilterCount > 0;
  const firstResult = data && data.totalElements > 0 ? data.number * data.size + 1 : 0;
  const lastResult = data ? Math.min((data.number + 1) * data.size, data.totalElements) : 0;

  return (
    <div className="results-page">
      <div className="results-header">
        <div>
          <p className="results-eyebrow">Doktor çalışma alanı</p>
          <h1 className="results-title">Hastalar</h1>
          <p className="results-subtitle">En son ölçümü olan hasta önce gösterilir.</p>
        </div>
        <div className="results-summary">
          <span className="results-count">{data ? data.totalElements.toLocaleString("tr-TR") : "—"}</span>
          <span>toplam hasta</span>
        </div>
      </div>

      <PatientSearchBar
        value={draftPatientId}
        onChange={setDraftPatientId}
        onApply={applyAllFilters}
        onClear={clearPatientSearch}
        isApplying={isFetching}
      />

      <PatientFilters
        values={draftFilters}
        activeCount={activeAdvancedFilterCount}
        isApplying={isFetching}
        onChange={setDraftFilters}
        onApply={applyAllFilters}
        onClear={clearAdvancedFilters}
      />

      {hasAnyFilter && (
        <div className="active-filter-row" aria-live="polite">
          <span>Liste filtreleniyor</span>
          {patientId && <strong>Hasta: {patientId}</strong>}
          {filters.testCode && <strong>Test: {filters.testCode}</strong>}
          {filters.status && <strong>Durum: {STATUS_LABELS[filters.status]}</strong>}
          {filters.from && <strong>Başlangıç: {filters.from}</strong>}
          {filters.to && <strong>Bitiş: {filters.to}</strong>}
          <button type="button" onClick={clearAllFilters}>Tümünü temizle</button>
        </div>
      )}

      {isError && (
        <p className="state-message" role="alert">
          Hastalar yüklenemedi: {(error as Error).message}
        </p>
      )}

      {isPending && <p className="state-message">Hastalar yükleniyor…</p>}

      {data && (
        <>
          <div className="results-toolbar">
            <p aria-live="polite">
              {data.totalElements === 0
                ? "Hasta bulunamadı"
                : `${firstResult}–${lastResult} arası gösteriliyor`}
              {isFetching && <span className="results-updating"> · Güncelleniyor…</span>}
            </p>
            <label>
              Sayfa başına
              <select value={pageSize} onChange={(e) => changePageSize(Number(e.target.value))}>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </label>
          </div>

          <div className="results-card">
            <div className="results-table-scroll">
              <table className="results-table">
                <thead>
                  <tr>
                    <th>Hasta</th>
                    <th>Test sayısı</th>
                    <th>Tüp sayısı</th>
                    <th>En kötü durum</th>
                    <th>Son ölçüm</th>
                    <th><span className="sr-only">Detay</span></th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((p) => (
                    <tr
                      key={p.patientId}
                      onClick={() => navigate(`/patients/${encodeURIComponent(p.patientId)}`)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter" || e.key === " ") {
                          e.preventDefault();
                          navigate(`/patients/${encodeURIComponent(p.patientId)}`);
                        }
                      }}
                      tabIndex={0}
                      className={`row-clickable ${p.worstStatus === "CRITICAL" ? "row-critical" : ""}`}
                    >
                      <td><strong>{p.patientId}</strong></td>
                      <td className="cell-value">{p.testCount}</td>
                      <td className="cell-value cell-muted">{p.sampleCount}</td>
                      <td><StatusBadge status={p.worstStatus} /></td>
                      <td className="cell-muted">{formatDate(p.lastMeasuredAt)}</td>
                      <td className="row-arrow" aria-hidden="true">›</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {data.totalPages > 1 && (
            <nav className="pagination" aria-label="Hasta sayfaları">
              <button
                type="button"
                onClick={() => setPage((current) => current - 1)}
                disabled={data.number === 0 || isFetching}
              >
                ← Önceki
              </button>
              <span>
                <strong>{data.number + 1}</strong> / {data.totalPages}. sayfa
              </span>
              <button
                type="button"
                onClick={() => setPage((current) => current + 1)}
                disabled={data.number + 1 >= data.totalPages || isFetching}
              >
                Sonraki →
              </button>
            </nav>
          )}
        </>
      )}

      {data && data.content.length === 0 && (
        <div className="empty-state">
          <strong>Bu aramayla eşleşen hasta bulunamadı.</strong>
          <p>Arama ve filtre değerlerini değiştirin veya tüm hastaları tekrar gösterin.</p>
          <button type="button" onClick={clearAllFilters}>Tüm hastaları göster</button>
        </div>
      )}
    </div>
  );
}
