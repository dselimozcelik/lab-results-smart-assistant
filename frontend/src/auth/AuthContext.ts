import { createContext } from "react";
import type { LoginResponse } from "../api/auth";

export type AuthSession = {
  username: string;
  role: string;
  expiresAt: number;
};

export type AuthContextValue = {
  session: AuthSession | null;
  sessionEndReason: "expired" | null;
  authenticate: (response: LoginResponse) => void;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);
