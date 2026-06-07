import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

export function RequireAuth() {
  const { session, sessionEndReason } = useAuth();
  const location = useLocation();

  if (!session) {
    const returnTo = `${location.pathname}${location.search}${location.hash}`;
    const params = new URLSearchParams({ from: returnTo });
    // "expired" = the session ran out mid-use. Otherwise the user reached a protected page with
    // no in-memory session (a fresh load / refresh / deep link), so explain that gently too.
    params.set("reason", sessionEndReason ?? "signin");
    return <Navigate to={`/login?${params}`} replace />;
  }
  return <Outlet />;
}
