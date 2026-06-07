import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

export function RequireAuth() {
  const { session, sessionEndReason } = useAuth();
  const location = useLocation();

  if (!session) {
    const returnTo = `${location.pathname}${location.search}${location.hash}`;
    const params = new URLSearchParams({ from: returnTo });
    if (sessionEndReason) {
      params.set("reason", sessionEndReason);
    }
    return <Navigate to={`/login?${params}`} replace />;
  }
  return <Outlet />;
}
