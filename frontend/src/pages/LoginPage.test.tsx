import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { login } from "../api/auth";
import { ApiError, setAuthToken } from "../api/client";
import { AuthProvider } from "../auth/AuthProvider";
import { LoginPage } from "./LoginPage";

vi.mock("../api/auth", () => ({
  login: vi.fn(),
}));

function renderLoginPage() {
  const queryClient = new QueryClient({
    defaultOptions: {
      mutations: { retry: false },
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    vi.mocked(login).mockReset();
    setAuthToken(null);
  });

  it("keeps submit disabled until both credentials are entered", async () => {
    const user = userEvent.setup();
    renderLoginPage();

    const submitButton = screen.getByRole("button", { name: "Giriş yap" });
    expect(submitButton).toBeDisabled();

    await user.type(screen.getByLabelText("Kullanıcı adı"), "doctor");
    expect(submitButton).toBeDisabled();

    await user.type(screen.getByLabelText("Şifre"), "doctor123");
    expect(submitButton).toBeEnabled();
  });

  it("shows a clear message when credentials are rejected", async () => {
    const user = userEvent.setup();
    vi.mocked(login).mockRejectedValue(new ApiError(401, "Unauthorized"));
    renderLoginPage();

    await user.type(screen.getByLabelText("Kullanıcı adı"), "doctor");
    await user.type(screen.getByLabelText("Şifre"), "wrong-password");
    await user.click(screen.getByRole("button", { name: "Giriş yap" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Kullanıcı adı veya şifre hatalı.",
    );
  });
});
