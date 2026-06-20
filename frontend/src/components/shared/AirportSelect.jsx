import React, { useEffect, useState } from "react";
import { PlaneLanding, MapPinned, AlertCircle, RotateCw } from "lucide-react";
import { searchAirports } from "../../api/api.js";

export default function AirportSelect({ destination, selectedAirportCode, onSelect }) {
  const [airports, setAirports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadToken, setReloadToken] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    searchAirports(destination.latitude, destination.longitude)
      .then((result) => {
        if (active) setAirports(result);
      })
      .catch((err) => {
        if (active) setError(err.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [destination.latitude, destination.longitude, reloadToken]);

  if (loading) {
    return (
      <div className="hotel-browser__status">
        <span className="spinner" /> Finding airports near {destination.name}...
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-banner">
        <AlertCircle size={16} /> {error}
        <button type="button" className="btn-replace" onClick={() => setReloadToken((t) => t + 1)}>
          <RotateCw size={13} /> Retry
        </button>
      </div>
    );
  }

  if (airports.length === 0) {
    return (
      <p className="wizard-step__subtitle">
        No commercial airports found near {destination.name}. Enter yours manually below.
      </p>
    );
  }

  return (
    <div className="hotel-grid">
      {airports.map((airport) => (
        <button
          key={airport.iataCode}
          className={`hotel-card airport-card ${selectedAirportCode === airport.iataCode ? "is-selected" : ""}`}
          onClick={() => onSelect(airport)}
        >
          <div className="hotel-card__body">
            <div className="hotel-card__name">
              <PlaneLanding size={15} /> {airport.name}
            </div>
            <div className="hotel-card__meta">
              <span className="tag">{airport.iataCode}</span>
              <span>
                <MapPinned size={13} /> {airport.distanceFromCityCenterKm} km from {destination.name}
              </span>
            </div>
          </div>
        </button>
      ))}
    </div>
  );
}
