import { Link, Outlet, useNavigate } from "react-router-dom";
import { Moon, Sun } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import { useTheme } from "../theme/useTheme";
import "./AppShell.css";

const ROLE_LABELS: Record<string, string> = {
  DOCTOR: "Doktor",
};

export function AppShell() {
  const { session, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header-inner">
          <Link to="/" className="app-brand" aria-label="Hasta listesine git">
            <span className="app-brand-mark">LR</span>
            <span>
              <strong>Lab Sonuçları</strong>
              <small>Akıllı Asistan</small>
            </span>
          </Link>

          <div className="app-user">
            <button
              type="button"
              className="app-theme-toggle"
              onClick={toggleTheme}
              aria-label={theme === "dark" ? "Açık temaya geç" : "Koyu temaya geç"}
              title={theme === "dark" ? "Açık tema" : "Koyu tema"}
            >
              {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="app-user-copy">
              <strong>{session?.username}</strong>
              <span>{ROLE_LABELS[session?.role ?? ""] ?? session?.role}</span>
            </div>
            <button type="button" onClick={handleLogout}>Çıkış yap</button>
          </div>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  );
}
