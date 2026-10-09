export type AuthUser = {
  id: number;
  username: string;
  nomComplet: string;
  role: "ADMIN" | "MEDECIN" | "SECRETAIRE";
};

type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: AuthUser;
};

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
let accessToken: string | null = null;
let refreshToken: string | null = null;

async function request<T>(path: string, init: RequestInit = {}, canRefresh = true): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...init.headers,
    },
  });
  if (response.status === 401 && canRefresh && refreshToken && path !== "/api/auth/refresh") {
    const renewed = await fetch(`${API_URL}/api/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
    if (renewed.ok) {
      const session = await renewed.json() as AuthResponse;
      accessToken = session.accessToken;
      refreshToken = session.refreshToken;
      return request<T>(path, init, false);
    }
    clearSession();
    window.location.hash = "session-expired";
    window.location.reload();
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new ApiError(response.status, body?.message ?? "API request failed");
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function authenticate(username: string, password: string): Promise<AuthUser> {
  const response = await request<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
  accessToken = response.accessToken;
  refreshToken = response.refreshToken;
  return response.user;
}

export async function logout(): Promise<void> {
  try {
    if (accessToken) await request<void>("/api/auth/logout", { method: "POST" });
  } finally {
    accessToken = null;
    refreshToken = null;
  }
}

export function clearSession(): void {
  accessToken = null;
  refreshToken = null;
}
