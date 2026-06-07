import type { LabResultFilters } from "../api/labResults";
import "./FilterBar.css";

type Props = {
  value: LabResultFilters;
  onChange: (next: LabResultFilters) => void;
};

export function FilterBar({ value, onChange }: Props) {
  function set(key: keyof LabResultFilters, v: string) {
    onChange({ ...value, [key]: v });
  }

  const hasAny = Object.values(value).some((v) => v);

  return (
    <div className="filter-bar">
      <div className="filter-field">
        <label htmlFor="f-patient">Hasta</label>
        <input
          id="f-patient"
          placeholder="P-800"
          value={value.patientId ?? ""}
          onChange={(e) => set("patientId", e.target.value)}
        />
      </div>

      <div className="filter-field">
        <label htmlFor="f-test">Test kodu</label>
        <input
          id="f-test"
          placeholder="GLU"
          value={value.testCode ?? ""}
          onChange={(e) => set("testCode", e.target.value)}
        />
      </div>

      <div className="filter-field">
        <label htmlFor="f-status">Durum</label>
        <select
          id="f-status"
          value={value.status ?? ""}
          onChange={(e) => set("status", e.target.value)}
        >
          <option value="">Tümü</option>
          <option value="NORMAL">Normal</option>
          <option value="LOW">Düşük</option>
          <option value="HIGH">Yüksek</option>
          <option value="CRITICAL">Kritik</option>
          <option value="INVALID">Geçersiz</option>
        </select>
      </div>

      <div className="filter-field">
        <label htmlFor="f-from">Başlangıç</label>
        <input
          id="f-from"
          type="datetime-local"
          value={value.from ?? ""}
          onChange={(e) => set("from", e.target.value)}
        />
      </div>

      <div className="filter-field">
        <label htmlFor="f-to">Bitiş</label>
        <input
          id="f-to"
          type="datetime-local"
          value={value.to ?? ""}
          onChange={(e) => set("to", e.target.value)}
        />
      </div>

      {hasAny && (
        <button type="button" className="filter-clear" onClick={() => onChange({})}>
          Temizle
        </button>
      )}
    </div>
  );
}
