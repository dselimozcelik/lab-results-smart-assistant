import { apiFetch } from "./client";

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  username: string;
  role: string;
  expiresInMinutes: number;
};

export function login(credentials: LoginRequest): Promise<LoginResponse> {
  return apiFetch<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: credentials,
  });
}
