import { apiFetch } from "./client";

export type AiAnalysis = {
  id: number;
  labResultId: number;
  model: string;
  promptVersion: string;
  summary: string;
  flaggedTests: string[];
  suggestedFollowups: string[];
  disclaimer: string;
  createdAt: string;
};

export function requestAiAnalysis(labResultId: number | string): Promise<AiAnalysis> {
  return apiFetch<AiAnalysis>(`/api/lab-results/${labResultId}/ai-analysis`, {
    method: "POST",
  });
}
