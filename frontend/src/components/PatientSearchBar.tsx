import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { usePatientSuggestions } from "../hooks/usePatientQueries";
import "./PatientSearchBar.css";

type Props = {
  value: string;
  onChange: (value: string) => void;
  onApply: () => void;
  onClear: () => void;
  isApplying: boolean;
};

export function PatientSearchBar({ value, onChange, onApply, onClear, isApplying }: Props) {
  const [suggestionQuery, setSuggestionQuery] = useState("");
  const [suggestionsOpen, setSuggestionsOpen] = useState(false);

  useEffect(() => {
    const trimmed = value.trim();
    const timeout = window.setTimeout(
      () => setSuggestionQuery(trimmed.length >= 2 ? trimmed : ""),
      250,
    );
    return () => window.clearTimeout(timeout);
  }, [value]);

  const suggestions = usePatientSuggestions(suggestionQuery, suggestionsOpen);

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setSuggestionsOpen(false);
    onApply();
  }

  return (
    <form className="patient-search" onSubmit={submit}>
      <div className="patient-search-copy">
        <h2>Hasta ara</h2>
        <p>Hasta numarasını yazın veya önerilerden seçin.</p>
      </div>

      <div className="patient-search-control">
        <label htmlFor="patient-search-input">Hasta numarası</label>
        <input
          id="patient-search-input"
          value={value}
          placeholder="Örn. P-800"
          role="combobox"
          aria-autocomplete="list"
          aria-expanded={suggestionsOpen && suggestionQuery.length >= 2}
          aria-controls="patient-search-suggestions"
          autoComplete="off"
          onFocus={() => setSuggestionsOpen(true)}
          onBlur={() => window.setTimeout(() => setSuggestionsOpen(false), 120)}
          onChange={(e) => {
            onChange(e.target.value);
            setSuggestionsOpen(true);
          }}
        />

        {suggestionsOpen && suggestionQuery.length >= 2 && (
          <div id="patient-search-suggestions" className="patient-suggestions" role="listbox">
            {suggestions.isFetching && <span>Hastalar aranıyor…</span>}
            {!suggestions.isFetching && suggestions.data?.map((patientId) => (
              <button
                key={patientId}
                type="button"
                role="option"
                aria-selected={patientId === value}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => {
                  onChange(patientId);
                  setSuggestionsOpen(false);
                }}
              >
                {patientId}
              </button>
            ))}
            {!suggestions.isFetching && suggestions.data?.length === 0 && (
              <span>Eşleşen hasta bulunamadı.</span>
            )}
          </div>
        )}
      </div>

      <div className="patient-search-actions">
        {value && (
          <button type="button" className="patient-search-clear" onClick={onClear}>
            Temizle
          </button>
        )}
        <button type="submit" className="patient-search-apply" disabled={isApplying}>
          {isApplying ? "Getiriliyor…" : "Hastaları getir"}
        </button>
      </div>
    </form>
  );
}
