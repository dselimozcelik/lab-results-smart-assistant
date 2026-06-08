import { useMutation } from "@tanstack/react-query";
import { AlertTriangle, ClipboardList, RotateCw, Sparkles } from "lucide-react";
import { requestAiAnalysis } from "../api/aiAnalysis";
import { useToast } from "../toast/useToast";
import { Skeleton } from "./Skeleton";
import "./AiAnalysisPanel.css";

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Bilinmeyen hata";
}

export function AiAnalysisPanel({ sampleId }: { sampleId: string }) {
  const { notify } = useToast();

  const mutation = useMutation({
    mutationFn: () => requestAiAnalysis(sampleId),
    onSuccess: () => notify("success", "Yapay zeka ön değerlendirmesi hazır."),
    onError: (error) => notify("error", `Analiz alınamadı: ${errorMessage(error)}`),
  });

  return (
    <section className="ai-panel">
      <div className="ai-panel-head">
        <h2 className="ai-panel-title">Yapay zeka ön değerlendirmesi</h2>
        <button
          className="ai-panel-button"
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending}
        >
          <Sparkles size={16} aria-hidden="true" />
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
          <p>
            <AlertTriangle size={15} aria-hidden="true" /> Analiz alınamadı: {errorMessage(mutation.error)}
          </p>
          <button type="button" onClick={() => mutation.mutate()}>
            <RotateCw size={14} aria-hidden="true" /> Tekrar dene
          </button>
        </div>
      )}

      {mutation.isSuccess && (
        <div className="ai-panel-result" aria-live="polite">
          <p className="ai-panel-summary">{mutation.data.summary}</p>

          <div className="ai-panel-blocks">
            {mutation.data.flaggedTests.length > 0 && (
              <div className="ai-panel-block">
                <h3><AlertTriangle size={13} aria-hidden="true" /> İşaretlenen testler</h3>
                <ul>
                  {mutation.data.flaggedTests.map((t, i) => <li key={i}>{t}</li>)}
                </ul>
              </div>
            )}

            {mutation.data.suggestedFollowups.length > 0 && (
              <div className="ai-panel-block">
                <h3><ClipboardList size={13} aria-hidden="true" /> Önerilen takip adımları</h3>
                <ul>
                  {mutation.data.suggestedFollowups.map((t, i) => <li key={i}>{t}</li>)}
                </ul>
              </div>
            )}
          </div>

          <p className="ai-panel-disclaimer">{mutation.data.disclaimer}</p>
          <p className="ai-panel-meta">Model: {mutation.data.model}</p>
        </div>
      )}
    </section>
  );
}
