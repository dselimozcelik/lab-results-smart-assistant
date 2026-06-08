import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { getPatient } from "../api/patients";
import type { SampleGroup } from "../api/patients";
import type { AnomalyStatus, LabResult } from "../api/labResults";
import { StatusBadge } from "../components/StatusBadge";
import { ReferenceRangeBar } from "../components/ReferenceRangeBar";
import { TestTrendSparkline } from "../components/TestTrendSparkline";
import { AiAnalysisPanel } from "../components/AiAnalysisPanel";
import { Skeleton } from "../components/Skeleton";
import "./PatientDetailPage.css";

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString("tr-TR");
}

// Most severe first so a doctor sees problems at the top of each tube.
const SEVERITY: Record<AnomalyStatus, number> = {
  CRITICAL: 4, HIGH: 3, LOW: 3, INVALID: 2, NORMAL: 1,
};

function abnormalFirst(tests: LabResult[]): LabResult[] {
  return [...tests].sort((a, b) => SEVERITY[b.anomalyStatus] - SEVERITY[a.anomalyStatus]);
}

function isAbnormal(status: AnomalyStatus): boolean {
  return status !== "NORMAL";
}

export function PatientDetailPage() {
  const { patientId } = useParams();

  const { data, isPending, isError, error, refetch } = useQuery({
    queryKey: ["patient", patientId],
    queryFn: () => getPatient(patientId!),
  });

  if (isPending) {
    return (
      <div className="patient-detail">
        <Link to="/" className="detail-back">← Hasta listesine dön</Link>
        <div className="patient-detail-header">
          <Skeleton width="80px" height="12px" />
          <Skeleton width="180px" height="30px" radius="var(--radius-sm)" />
        </div>
        <section className="tube-card">
          <Skeleton width="40%" height="18px" />
          <div style={{ marginTop: "var(--sp-4)", display: "grid", gap: "var(--sp-3)" }}>
            <Skeleton height="14px" />
            <Skeleton width="90%" height="14px" />
            <Skeleton width="95%" height="14px" />
          </div>
        </section>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="patient-detail">
        <Link to="/" className="detail-back">← Hasta listesine dön</Link>
        <div className="state-error" role="alert">
          <p>Hasta bulunamadı: {(error as Error).message}</p>
          <button type="button" onClick={() => refetch()}>Tekrar dene</button>
        </div>
      </div>
    );
  }

  return (
    <div className="patient-detail">
      <Link to="/" className="detail-back">← Hasta listesine dön</Link>

      <div className="patient-detail-header">
        <p className="detail-eyebrow">Hasta</p>
        <h1 className="detail-title">{data.patientId}</h1>
        <p className="patient-detail-sub">{data.samples.length} numune (tüp)</p>
      </div>

      {data.samples.map((sample) => (
        <TubeCard key={sample.sampleId} patientId={data.patientId} sample={sample} />
      ))}
    </div>
  );
}

function TubeCard({ patientId, sample }: { patientId: string; sample: SampleGroup }) {
  const tests = abnormalFirst(sample.tests);

  return (
    <section className="tube-card">
      <div className="tube-head">
        <div>
          <h2 className="tube-title">Tüp {sample.sampleId}</h2>
          <p className="tube-meta">{formatDate(sample.measuredAt)} · {sample.deviceId}</p>
        </div>
        <StatusBadge status={sample.worstStatus} />
      </div>

      <div className="tube-table-scroll">
        <table className="tube-table">
          <thead>
            <tr>
              <th>Test</th>
              <th>Değer</th>
              <th>Referans aralığı</th>
              <th>Trend</th>
              <th>Durum</th>
            </tr>
          </thead>
          <tbody>
            {tests.map((t) => (
              <tr key={t.id} className={isAbnormal(t.anomalyStatus) ? "row-abnormal" : ""}>
                <td>
                  <strong>{t.testName}</strong>
                  <span className="cell-secondary">{t.testCode}</span>
                </td>
                <td className="cell-value">
                  {t.value ?? "—"} <span className="cell-muted">{t.unit ?? ""}</span>
                </td>
                <td>
                  <ReferenceRangeBar
                    value={t.value}
                    referenceMin={t.referenceMin}
                    referenceMax={t.referenceMax}
                    status={t.anomalyStatus}
                  />
                </td>
                <td>
                  <TestTrendSparkline
                    patientId={patientId}
                    testCode={t.testCode}
                    referenceMin={t.referenceMin}
                    referenceMax={t.referenceMax}
                  />
                </td>
                <td><StatusBadge status={t.anomalyStatus} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <AiAnalysisPanel sampleId={sample.sampleId} />
    </section>
  );
}
