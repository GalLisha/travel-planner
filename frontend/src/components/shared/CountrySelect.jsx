import React, { useEffect, useRef, useState } from "react";
import { ChevronDown, Search } from "lucide-react";

export default function CountrySelect({ value, onChange, countries, placeholder = "Any country" }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const containerRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    function handleOutsideClick(e) {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, []);

  useEffect(() => {
    if (open) {
      setQuery("");
      inputRef.current?.focus();
    }
  }, [open]);

  const selected = countries.find((c) => c.code === value);
  const filtered = query.trim()
    ? countries.filter((c) => c.name.toLowerCase().includes(query.trim().toLowerCase()))
    : countries;

  function handleSelect(code) {
    onChange(code);
    setOpen(false);
  }

  return (
    <div className="country-select" ref={containerRef}>
      <button type="button" className="country-select__trigger" onClick={() => setOpen((o) => !o)}>
        <span className={selected ? "" : "hint"}>{selected ? selected.name : placeholder}</span>
        <ChevronDown size={16} />
      </button>

      {open && (
        <div className="country-select__dropdown">
          <div className="country-select__search">
            <Search size={14} />
            <input
              ref={inputRef}
              type="text"
              placeholder="Search countries..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>
          <div className="country-select__list">
            <button
              type="button"
              className={`country-select__option ${!value ? "is-selected" : ""}`}
              onClick={() => handleSelect("")}
            >
              Any country
            </button>
            {filtered.map((c) => (
              <button
                key={c.code}
                type="button"
                className={`country-select__option ${value === c.code ? "is-selected" : ""}`}
                onClick={() => handleSelect(c.code)}
              >
                {c.name}
              </button>
            ))}
            {filtered.length === 0 && <div className="city-search__empty">No countries found.</div>}
          </div>
        </div>
      )}
    </div>
  );
}
