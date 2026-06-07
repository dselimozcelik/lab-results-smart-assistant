import { Navigate } from "react-router-dom";
import { getAuthToken } from "../api/client";

export function RequireAuth({ children }: { children: React.ReactNode }) {
  if (!getAuthToken()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}
