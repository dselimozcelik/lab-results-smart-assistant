import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { ToastProvider } from "./ToastProvider";
import { useToast } from "./useToast";

function ToastProbe() {
  const { notify } = useToast();
  return (
    <button onClick={() => notify("success", "Kaydedildi")}>Bildir</button>
  );
}

function renderWithToasts() {
  return render(
    <ToastProvider>
      <ToastProbe />
    </ToastProvider>,
  );
}

describe("ToastProvider", () => {
  it("shows a toast when notify is called", async () => {
    const user = userEvent.setup();
    renderWithToasts();

    await user.click(screen.getByRole("button", { name: "Bildir" }));

    expect(screen.getByText("Kaydedildi")).toBeVisible();
  });

  it("dismisses a toast when its close button is clicked", async () => {
    const user = userEvent.setup();
    renderWithToasts();

    await user.click(screen.getByRole("button", { name: "Bildir" }));
    await user.click(screen.getByRole("button", { name: "Bildirimi kapat" }));

    expect(screen.queryByText("Kaydedildi")).not.toBeInTheDocument();
  });
});
