// Refetch/stale cadence, build-time configurable via VITE_REFETCH_MS, default 30s.
export const REFETCH_MS = Number(import.meta.env.VITE_REFETCH_MS) || 30_000;
