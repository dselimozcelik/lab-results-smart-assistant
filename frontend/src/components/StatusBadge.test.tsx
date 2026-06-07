import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StatusBadge } from "./StatusBadge";

describe("StatusBadge", () => {
  it("communicates a critical result with text and styling", () => {
    render(<StatusBadge status="CRITICAL" />);

    const badge = screen.getByText("Kritik");
    expect(badge).toHaveClass("badge--critical");
  });
});
