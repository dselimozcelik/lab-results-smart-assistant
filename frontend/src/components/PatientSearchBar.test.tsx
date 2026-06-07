import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getPatientSuggestions } from "../api/patients";
import { PatientSearchBar } from "./PatientSearchBar";

vi.mock("../api/patients", () => ({
  getPatientSuggestions: vi.fn(),
}));

describe("PatientSearchBar", () => {
  beforeEach(() => {
    vi.mocked(getPatientSuggestions).mockResolvedValue(["P-389", "P-390"]);
  });

  it("offers case-insensitive suggestions without applying the list filter", async () => {
    const onChange = vi.fn();
    const onApply = vi.fn();
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });

    render(
      <QueryClientProvider client={queryClient}>
        <PatientSearchBar
          value="p-"
          onChange={onChange}
          onApply={onApply}
          onClear={vi.fn()}
          isApplying={false}
        />
      </QueryClientProvider>,
    );

    fireEvent.focus(screen.getByRole("combobox"));

    await waitFor(() => expect(getPatientSuggestions).toHaveBeenCalledWith("p-"));
    fireEvent.click(await screen.findByRole("option", { name: "P-389" }));

    expect(onChange).toHaveBeenCalledWith("P-389");
    expect(onApply).not.toHaveBeenCalled();
  });
});
