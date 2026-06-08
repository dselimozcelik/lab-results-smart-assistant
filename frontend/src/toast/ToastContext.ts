import { createContext } from "react";

export type ToastKind = "success" | "error" | "info";

export type Toast = {
  id: number;
  kind: ToastKind;
  message: string;
};

export type ToastContextValue = {
  // Show a transient notification; returns nothing, auto-dismisses.
  notify: (kind: ToastKind, message: string) => void;
};

export const ToastContext = createContext<ToastContextValue | null>(null);
