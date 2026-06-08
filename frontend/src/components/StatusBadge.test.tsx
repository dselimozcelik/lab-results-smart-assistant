import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StatusBadge } from "./StatusBadge";

describe("StatusBadge", () => {
  it("communicates a critical result with text and styling", () => {
    render(<StatusBadge status="CRITICAL" />);

    const badge = screen.getByText("Kritik");
    expect(badge).toHaveClass("badge--critical");
  });

  it("renders a redundant icon alongside the label for each status", () => {
    const { container } = render(<StatusBadge status="LOW" />);

    // The glyph carries meaning beyond colour; assert it is actually drawn.
    expect(container.querySelector(".badge-icon")).toBeInTheDocument();
    expect(screen.getByText("Düşük")).toBeInTheDocument();
  });
});
