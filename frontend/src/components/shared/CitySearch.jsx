import React, { useEffect, useRef, useState } from "react";
import { Search, MapPin, Loader2, Info } from "lucide-react";
import { searchCities } from "../../api/api.js";
import { useDebouncedValue } from "../../utils/useDebouncedValue.js";

export default function CitySearch({ countryCode, onSelect, placeholder = "Search for a city..." }) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [open, setOpen] = useState(false);
  const debouncedQuery = useDebouncedValue(query, 300);
  const abortRef = useRef(null);
  const containerRef = useRef(null);

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
    if (debouncedQuery.trim().length < 2) {
      setResults([]);
      setLoading(false);
      setError(null);
      return;
    }

    if (abortRef.current) {
      abortRef.current.abort();
    }
    const controller = new AbortController();
    abortRef.current = controller;

    setLoading(true);
    setError(null);
    searchCities(debouncedQuery.trim(), countryCode, controller.signal)
      .then((cities) => {
        setResults(cities);
        setOpen(true);
      })
      .catch((err) => {
        if (err.name === "AbortError") return;
        setError(err.message);
        setResults([]);
        setOpen(true);
      })
      .finally(() => setLoading(false));

    return () => controller.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedQuery, countryCode]);

  function handleSelect(city) {
    setQuery(`${city.name}, ${city.country}`);
    setOpen(false);
    onSelect(city);
  }

  const showDropdown = open && query.trim().length >= 2;

  return (
    <div className="city-search" ref={containerRef}>
      <div className="search-box">
        {loading ? <Loader2 size={16} className="spin-icon" /> : <Search size={16} />}
        <input
          type="text"
          placeholder={placeholder}
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setOpen(true);
          }}
          onFocus={() => query.trim().length >= 2 && setOpen(true)}
        />
      </div>

      {showDropdown && (
        <div className="city-search__dropdown">
          {error && (
            <div className="error-banner" style={{ margin: "0.5rem" }}>
              <Info size={16} /> {error}
            </div>
          )}
          {!error && !loading && results.length === 0 && (
            <div className="city-search__empty">No cities found. Try a different spelling.</div>
          )}
          {results.map((city, idx) => (
            <button key={`${city.name}-${city.country}-${idx}`} className="city-search__option" onClick={() => handleSelect(city)}>
              <MapPin size={15} />
              <span className="city-search__option-text">
                <strong>{city.name}</strong>
                <span className="hint">
                  {city.region ? `${city.region}, ` : ""}{city.country}
                  {city.population ? ` · pop. ${city.population.toLocaleString()}` : ""}
                </span>
              </span>
              {city.curatedDestinationId && <span className="tag">Full itinerary</span>}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
