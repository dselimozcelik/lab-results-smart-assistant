import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { requestAiAnalysis } from "../api/aiAnalysis";
import { ToastProvider } from "../toast/ToastProvider";
import { AiAnalysisPanel } from "./AiAnalysisPanel";

vi.mock("../api/aiAnalysis", () => ({
  requestAiAnalysis: vi.fn(),
}));

function renderPanel() {
  const queryClient = new QueryClient({
    defaultOptions: {
      mutations: { retry: false },
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <AiAnalysisPanel sampleId="SAMPLE-1" />
      </ToastProvider>
    </QueryClientProvider>,
  );
}

describe("AiAnalysisPanel", () => {
  beforeEach(() => {
    vi.mocked(requestAiAnalysis).mockReset();
  });

  it("disables the button while analysis is running", async () => {
    const user = userEvent.setup();
    vi.mocked(requestAiAnalysis).mockReturnValue(new Promise(() => undefined));
    renderPanel();

    await user.click(screen.getByRole("button", { name: "AI analizi al" }));

    expect(screen.getByRole("button", { name: "Analiz ediliyor…" })).toBeDisabled();
  });

  it("renders the returned analysis and enforced disclaimer", async () => {
    const user = userEvent.setup();
    vi.mocked(requestAiAnalysis).mockResolvedValue({
      id: 1,
      sampleFk: 2,
      model: "llama3.2",
      promptVersion: "v1",
      summary: "Hemoglobin değeri referans aralığının altında.",
      flaggedTests: ["HGB: LOW"],
      suggestedFollowups: ["Klinik bulgularla birlikte değerlendirin."],
      disclaimer: "Bu çıktı tanı değildir.",
      createdAt: "2026-06-08T10:00:00Z",
    });
    renderPanel();

    await user.click(screen.getByRole("button", { name: "AI analizi al" }));

    expect(await screen.findByText("Hemoglobin değeri referans aralığının altında.")).toBeVisible();
    expect(screen.getByText("HGB: LOW")).toBeVisible();
    expect(screen.getByText("Bu çıktı tanı değildir.")).toBeVisible();
  });

  it("shows an actionable error when analysis fails", async () => {
    const user = userEvent.setup();
    vi.mocked(requestAiAnalysis).mockRejectedValue(new Error("Ollama yanıt vermedi"));
    renderPanel();

    await user.click(screen.getByRole("button", { name: "AI analizi al" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Analiz alınamadı: Ollama yanıt vermedi",
    );
  });
});
