import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { login } from "../api/auth";
import { ApiError } from "../api/client";
import { useAuth } from "../auth/useAuth";
import "./LoginPage.css";

export function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { authenticate, session } = useAuth();
  const requestedPath = searchParams.get("from");
  const returnTo = requestedPath?.startsWith("/") && !requestedPath.startsWith("//")
    ? requestedPath
    : "/";

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      authenticate(data);
      navigate(returnTo, { replace: true });
    },
  });

  if (session) {
    return <Navigate to={returnTo} replace />;
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    mutation.mutate({ username: username.trim(), password });
  }

  const isIncomplete = !username.trim() || !password;
  const errorMessage = mutation.error instanceof ApiError && mutation.error.status === 401
    ? "Kullanıcı adı veya şifre hatalı."
    : "Giriş şu anda tamamlanamadı. Lütfen tekrar deneyin.";

  return (
    <div className="login-screen">
      <form className="login-card" onSubmit={handleSubmit}>
        <p className="login-brand">LAB RESULTS · SMART ASSISTANT</p>
        <h1 className="login-title">Giriş</h1>
        <p className="login-subtitle">Sonuçları görüntülemek için oturum açın.</p>

        {searchParams.get("reason") === "expired" && (
          <p className="login-notice" role="status">
            Oturumunuz sona erdi. Devam etmek için tekrar giriş yapın.
          </p>
        )}

        <div className="login-field">
          <label htmlFor="username">Kullanıcı adı</label>
          <input
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
          />
        </div>

        <div className="login-field">
          <label htmlFor="password">Şifre</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </div>

        {mutation.isError && (
          <p className="login-error" role="alert">
            {errorMessage}
          </p>
        )}

        <button className="login-button" type="submit" disabled={mutation.isPending || isIncomplete}>
          {mutation.isPending ? "Giriş yapılıyor…" : "Giriş yap"}
        </button>
      </form>
    </div>
  );
}
