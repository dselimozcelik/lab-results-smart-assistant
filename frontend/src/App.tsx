import { Routes, Route, Navigate } from "react-router-dom";
import { LoginPage } from "./pages/LoginPage";
import { ResultsPage } from "./pages/ResultsPage";
import { DetailPage } from "./pages/DetailPage";
import { RequireAuth } from "./components/RequireAuth";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <ResultsPage />
          </RequireAuth>
        }
      />
      <Route
        path="/results/:id"
        element={
          <RequireAuth>
            <DetailPage />
          </RequireAuth>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
