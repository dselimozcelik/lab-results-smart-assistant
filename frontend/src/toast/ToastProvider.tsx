import { useCallback, useRef, useState, type ReactNode } from "react";
import { AlertCircle, CheckCircle2, Info, X } from "lucide-react";
import { ToastContext, type Toast, type ToastKind } from "./ToastContext";
import "./Toast.css";

const AUTO_DISMISS_MS = 4500;

const ICONS = {
  success: CheckCircle2,
  error: AlertCircle,
  info: Info,
} as const;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const nextId = useRef(1);

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((t) => t.id !== id));
  }, []);

  const notify = useCallback(
    (kind: ToastKind, message: string) => {
      const id = nextId.current++;
      setToasts((current) => [...current, { id, kind, message }]);
      window.setTimeout(() => dismiss(id), AUTO_DISMISS_MS);
    },
    [dismiss],
  );

  return (
    <ToastContext.Provider value={{ notify }}>
      {children}
      {/* Polite live region: new toasts are announced without stealing focus. */}
      <div className="toast-viewport" role="region" aria-live="polite" aria-label="Bildirimler">
        {toasts.map((toast) => {
          const Icon = ICONS[toast.kind];
          return (
            <div key={toast.id} className={`toast toast--${toast.kind}`}>
              <Icon className="toast-icon" size={18} aria-hidden="true" />
              <span className="toast-message">{toast.message}</span>
              <button
                type="button"
                className="toast-close"
                onClick={() => dismiss(toast.id)}
                aria-label="Bildirimi kapat"
              >
                <X size={15} aria-hidden="true" />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}
