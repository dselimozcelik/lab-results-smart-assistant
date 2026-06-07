import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, within } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getPatients } from "../api/patients";
import { PatientsPage } from "./PatientsPage";

vi.mock("../api/patients", () => ({
  getPatients: vi.fn(),
  getPatientSuggestions: vi.fn(),
}));

describe("PatientsPage", () => {
  beforeEach(() => {
    vi.mocked(getPatients).mockResolvedValue({
      content: [{
        patientId: "P-389",
        testCount: 4,
        sampleCount: 1,
        worstStatus: "CRITICAL",
        lastMeasuredAt: "2026-06-08T10:00:00Z",
      }],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 20,
    });
  });

  it("renders patients and opens the selected patient detail route", async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={["/"]}>
          <Routes>
            <Route path="/" element={<PatientsPage />} />
            <Route path="/patients/:patientId" element={<p>Hasta detay rotası</p>} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    );

    const patientId = await screen.findByText("P-389");
    const row = patientId.closest("tr")!;
    expect(within(row).getByText("Kritik")).toBeVisible();

    fireEvent.click(row);

    expect(screen.getByText("Hasta detay rotası")).toBeVisible();
  });
});
