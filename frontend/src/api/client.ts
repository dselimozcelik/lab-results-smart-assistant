const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

let authToken: string | null = null;
let unauthorizedHandler: (() => void) | null = null;

export function setAuthToken(token: string | null): void {
  authToken = token;
}

export function getAuthToken(): string | null {
  return authToken;
}

export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

type Options = {
  method?: string;
  body?: unknown;
};

export async function apiFetch<T>(path: string, options: Options = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (authToken) {
    headers["Authorization"] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  // fetch does not throw on 4xx/5xx; surface them as a typed error.
  if (!response.ok) {
    const error = new ApiError(response.status, await readError(response));
    if (response.status === 401 && authToken) {
      unauthorizedHandler?.();
    }
    throw error;
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

async function readError(response: Response): Promise<string> {
  try {
    const problem = await response.json();
    return problem.detail ?? problem.title ?? response.statusText;
  } catch {
    return response.statusText;
  }
}
