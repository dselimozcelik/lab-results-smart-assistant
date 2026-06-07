import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { getPatients } from "../api/patients";
import { PatientSearchBar } from "../components/PatientSearchBar";
import { StatusBadge } from "../components/StatusBadge";
import "./ResultsPage.css";

const DEFAULT_PAGE_SIZE = 20;

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString("tr-TR");
}

export function PatientsPage() {
  const [draftPatientId, setDraftPatientId] = useState("");
  const [patientId, setPatientId] = useState("");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const navigate = useNavigate();

  const query = { patientId: patientId || undefined, page, size: pageSize };

  const { data, isPending, isFetching, isError, error } = useQuery({
    queryKey: ["patients", query],
    queryFn: () => getPatients(query),
    placeholderData: keepPreviousData,
  });

  function changePageSize(next: number) {
    setPageSize(next);
    setPage(0);
  }

  function applyPatientSearch() {
    setPatientId(draftPatientId.trim());
    setPage(0);
  }

  function clearPatientSearch() {
    setDraftPatientId("");
    setPatientId("");
    setPage(0);
  }

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
        onApply={applyPatientSearch}
        onClear={clearPatientSearch}
        isApplying={isFetching}
      />

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
          <p>Hasta numarasını değiştirin veya tüm hastaları tekrar gösterin.</p>
          <button type="button" onClick={clearPatientSearch}>Tüm hastaları göster</button>
        </div>
      )}
    </div>
  );
}
