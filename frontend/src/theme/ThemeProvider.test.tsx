import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ThemeProvider } from "./ThemeProvider";
import { useTheme } from "./useTheme";

// jsdom has no matchMedia; default it to "light preference" unless a test overrides it.
function mockMatchMedia(prefersDark: boolean) {
  vi.stubGlobal(
    "matchMedia",
    vi.fn().mockReturnValue({
      matches: prefersDark,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    }),
  );
}

function ThemeProbe() {
  const { theme, toggleTheme } = useTheme();
  return (
    <button onClick={toggleTheme}>theme:{theme}</button>
  );
}

describe("ThemeProvider", () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute("data-theme");
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("falls back to the OS preference when nothing is saved", () => {
    mockMatchMedia(true);

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(screen.getByRole("button")).toHaveTextContent("theme:dark");
    expect(document.documentElement.getAttribute("data-theme")).toBe("dark");
  });

  it("prefers a saved choice over the OS preference", () => {
    mockMatchMedia(true);
    localStorage.setItem("lab-theme", "light");

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(screen.getByRole("button")).toHaveTextContent("theme:light");
  });

  it("toggles and persists the choice", async () => {
    mockMatchMedia(false);
    const user = userEvent.setup();

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    await user.click(screen.getByRole("button"));

    expect(screen.getByRole("button")).toHaveTextContent("theme:dark");
    expect(localStorage.getItem("lab-theme")).toBe("dark");
    expect(document.documentElement.getAttribute("data-theme")).toBe("dark");
  });
});
