import { useMutation } from "@tanstack/react-query";
import { requestAiAnalysis } from "../api/aiAnalysis";
import { Skeleton } from "./Skeleton";
import "./AiAnalysisPanel.css";

export function AiAnalysisPanel({ sampleId }: { sampleId: string }) {
  const mutation = useMutation({
    mutationFn: () => requestAiAnalysis(sampleId),
  });

  return (
    <section className="ai-panel">
      <div className="ai-panel-head">
        <h2 className="ai-panel-title">Yapay zekâ ön değerlendirmesi</h2>
        <button
          className="ai-panel-button"
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Analiz ediliyor…" : "AI analizi al"}
        </button>
      </div>

      {mutation.isPending && (
        <div className="ai-panel-loading" role="status" aria-label="Analiz ediliyor">
          <Skeleton width="95%" height="13px" />
          <Skeleton width="100%" height="13px" />
          <Skeleton width="80%" height="13px" />
        </div>
      )}

      {mutation.isError && (
        <div className="ai-panel-error" role="alert">
          <p>Analiz alınamadı: {(mutation.error as Error).message}</p>
          <button type="button" onClick={() => mutation.mutate()}>Tekrar dene</button>
        </div>
      )}

      {mutation.isSuccess && (
        <div className="ai-panel-result">
          <p className="ai-panel-summary">{mutation.data.summary}</p>

          {mutation.data.flaggedTests.length > 0 && (
            <div className="ai-panel-block">
              <h3>İşaretlenen testler</h3>
              <ul>
                {mutation.data.flaggedTests.map((t, i) => <li key={i}>{t}</li>)}
              </ul>
            </div>
          )}

          {mutation.data.suggestedFollowups.length > 0 && (
            <div className="ai-panel-block">
              <h3>Önerilen takip adımları</h3>
              <ul>
                {mutation.data.suggestedFollowups.map((t, i) => <li key={i}>{t}</li>)}
              </ul>
            </div>
          )}

          <p className="ai-panel-disclaimer">{mutation.data.disclaimer}</p>
          <p className="ai-panel-meta">Model: {mutation.data.model}</p>
        </div>
      )}
    </section>
  );
}
