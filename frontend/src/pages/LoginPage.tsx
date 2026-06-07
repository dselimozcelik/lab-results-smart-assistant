import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { login } from "../api/auth";
import { setAuthToken } from "../api/client";
import "./LoginPage.css";

export function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      setAuthToken(data.token);
      navigate("/");
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    mutation.mutate({ username, password });
  }

  return (
    <div className="login-screen">
      <form className="login-card" onSubmit={handleSubmit}>
        <p className="login-brand">LAB RESULTS · SMART ASSISTANT</p>
        <h1 className="login-title">Giriş</h1>
        <p className="login-subtitle">Sonuçları görüntülemek için oturum açın.</p>

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
            {(mutation.error as Error).message}
          </p>
        )}

        <button className="login-button" type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Giriş yapılıyor…" : "Giriş yap"}
        </button>
      </form>
    </div>
  );
}
