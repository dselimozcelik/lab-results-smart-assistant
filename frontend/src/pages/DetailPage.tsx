import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { getLabResult } from "../api/labResults";
import { StatusBadge } from "../components/StatusBadge";
import { AiAnalysisPanel } from "../components/AiAnalysisPanel";
import "./DetailPage.css";

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString("tr-TR");
}

export function DetailPage() {
  const { id } = useParams();

  const { data, isPending, isError, error } = useQuery({
    queryKey: ["labResult", id],
    queryFn: () => getLabResult(id!),
  });

  if (isPending) {
    return <p className="state-message">Yükleniyor…</p>;
  }

  if (isError) {
    return (
      <div className="detail-page">
        <Link to="/" className="detail-back">← Listeye dön</Link>
        <p className="state-message" role="alert">
          Sonuç bulunamadı: {(error as Error).message}
        </p>
      </div>
    );
  }

  return (
    <div className="detail-page">
      <Link to="/" className="detail-back">← Listeye dön</Link>

      <div className="detail-header">
        <div>
          <p className="detail-eyebrow">{data.patientId} · {data.sampleId}</p>
          <h1 className="detail-title">{data.testName}</h1>
        </div>
        <StatusBadge status={data.anomalyStatus} />
      </div>

      <div className="detail-card">
        <dl className="detail-grid">
          <div><dt>Değer</dt><dd className="cell-value">{data.value ?? "—"} {data.unit ?? ""}</dd></div>
          <div><dt>Referans aralığı</dt><dd className="cell-value">{data.referenceMin ?? "—"} – {data.referenceMax ?? "—"} {data.unit ?? ""}</dd></div>
          <div><dt>Test kodu</dt><dd>{data.testCode}</dd></div>
          <div><dt>Cihaz</dt><dd>{data.deviceId}</dd></div>
          <div><dt>Ölçüm zamanı</dt><dd>{formatDate(data.measuredAt)}</dd></div>
          <div><dt>Kaydedilme</dt><dd>{formatDate(data.createdAt)}</dd></div>
        </dl>
      </div>

      <AiAnalysisPanel labResultId={data.id} />
    </div>
  );
}
