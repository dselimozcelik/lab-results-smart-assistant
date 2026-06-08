import type { AnomalyStatus } from "../api/labResults";
import { STATUS_LABELS } from "../utils/labUtils";
import "./PatientFilters.css";

export type PatientFilterValues = {
  testCode: string;
  status: "" | AnomalyStatus;
  from: string;
  to: string;
};

type Props = {
  values: PatientFilterValues;
  activeCount: number;
  isApplying: boolean;
  onChange: (values: PatientFilterValues) => void;
  onApply: () => void;
  onClear: () => void;
};

// Derived from the shared labels so the option list can never drift from the badge wording.
const STATUS_OPTIONS = (Object.entries(STATUS_LABELS) as Array<[AnomalyStatus, string]>).map(
  ([value, label]) => ({ value, label }),
);

export function PatientFilters({
  values,
  activeCount,
  isApplying,
  onChange,
  onApply,
  onClear,
}: Props) {
  return (
    <details className="patient-filters" open={activeCount > 0}>
      <summary>
        <span>Gelişmiş filtreler</span>
        <span className="filter-summary-copy">
          {activeCount > 0 ? `${activeCount} filtre aktif` : "Test, durum veya tarihe göre daraltın"}
        </span>
      </summary>

      <div className="filter-fields">
        <label>
          <span>Test kodu</span>
          <input
            value={values.testCode}
            placeholder="Örn. GLU"
            onChange={(event) => onChange({ ...values, testCode: event.target.value })}
          />
        </label>

        <label>
          <span>Sonuç durumu</span>
          <select
            value={values.status}
            onChange={(event) => onChange({
              ...values,
              status: event.target.value as PatientFilterValues["status"],
            })}
          >
            <option value="">Tüm durumlar</option>
            {STATUS_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </label>

        <label>
          <span>Başlangıç tarihi</span>
          <input
            type="date"
            value={values.from}
            max={values.to || undefined}
            onChange={(event) => onChange({ ...values, from: event.target.value })}
          />
        </label>

        <label>
          <span>Bitiş tarihi</span>
          <input
            type="date"
            value={values.to}
            min={values.from || undefined}
            onChange={(event) => onChange({ ...values, to: event.target.value })}
          />
        </label>
      </div>

      <div className="filter-actions">
        {activeCount > 0 && (
          <button type="button" className="filter-clear" onClick={onClear}>Filtreleri temizle</button>
        )}
        <button type="button" className="filter-apply" disabled={isApplying} onClick={onApply}>
          {isApplying ? "Uygulanıyor…" : "Filtreleri uygula"}
        </button>
      </div>
    </details>
  );
}
