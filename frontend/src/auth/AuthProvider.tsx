import { useCallback, useEffect, useState } from "react";
import type { ReactNode } from "react";
import { useQueryClient } from "@tanstack/react-query";
import type { LoginResponse } from "../api/auth";
import { setAuthToken, setUnauthorizedHandler } from "../api/client";
import { AuthContext } from "./AuthContext";
import type { AuthSession } from "./AuthContext";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [sessionEndReason, setSessionEndReason] = useState<"expired" | null>(null);
  const queryClient = useQueryClient();

  const logout = useCallback(() => {
    setAuthToken(null);
    setSession(null);
    setSessionEndReason(null);
    queryClient.clear();
  }, [queryClient]);

  const expireSession = useCallback(() => {
    setAuthToken(null);
    setSession(null);
    setSessionEndReason("expired");
    queryClient.clear();
  }, [queryClient]);

  const authenticate = useCallback((response: LoginResponse) => {
    setAuthToken(response.token);
    setSessionEndReason(null);
    setSession({
      username: response.username,
      role: response.role,
      expiresAt: Date.now() + response.expiresInMinutes * 60_000,
    });
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(expireSession);
    return () => setUnauthorizedHandler(null);
  }, [expireSession]);

  useEffect(() => {
    if (!session) {
      return;
    }
    const timeout = window.setTimeout(expireSession, Math.max(0, session.expiresAt - Date.now()));
    return () => window.clearTimeout(timeout);
  }, [expireSession, session]);

  return (
    <AuthContext.Provider value={{ session, sessionEndReason, authenticate, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
