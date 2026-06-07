import { apiFetch } from "./client";

export type AiAnalysis = {
  id: number;
  sampleFk: number;
  model: string;
  promptVersion: string;
  summary: string;
  flaggedTests: string[];
  suggestedFollowups: string[];
  disclaimer: string;
  createdAt: string;
};

// Analysis is per tube (sample): the whole panel is reviewed together.
export function requestAiAnalysis(sampleId: string): Promise<AiAnalysis> {
  return apiFetch<AiAnalysis>(`/api/samples/${encodeURIComponent(sampleId)}/ai-analysis`, {
    method: "POST",
  });
}
