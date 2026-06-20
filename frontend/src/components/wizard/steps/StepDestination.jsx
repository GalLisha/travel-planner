import React, { useState } from "react";
import { MapPin, Sparkles, ArrowLeft, Info } from "lucide-react";
import { useWizardDispatch } from "../../../context/WizardContext.jsx";
import { suggestDestinations } from "../../../api/api.js";
import DestinationGrid from "../../shared/DestinationGrid.jsx";
import CitySearch from "../../shared/CitySearch.jsx";
import { COUNTRIES } from "../../../data/countries.js";
import { label } from "../../../utils/format.js";

const BUDGET_LEVELS = ["LOW", "MEDIUM", "HIGH", "LUXURY"];
const REGIONS = ["EUROPE", "ASIA", "NORTH_AMERICA", "SOUTH_AMERICA", "AFRICA", "OCEANIA", "MIDDLE_EAST", "CARIBBEAN"];
const VACATION_STYLES = ["RELAXATION", "ADVENTURE", "CULTURE", "NATURE", "NIGHTLIFE", "ROMANTIC", "FAMILY_FUN", "CITY_BREAK"];
const ACTIVITIES = ["BEACH", "HIKING", "MUSEUMS", "SHOPPING", "NIGHTLIFE", "FOOD", "HISTORY", "THEME_PARKS", "WATER_SPORTS", "WILDLIFE", "ART", "LANDMARKS"];

export default function StepDestination() {
  const dispatch = useWizardDispatch();
  const [phase, setPhase] = useState("choice"); // choice | known | suggestForm | suggestResults
  const [countryFilter, setCountryFilter] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [departureLocation, setDepartureLocation] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [prefs, setPrefs] = useState({
    budgetLevel: "",
    region: "",
    maxFlightDurationHours: "",
    vacationStyles: [],
    activities: [],
    additionalPreferences: "",
  });

  function handleCitySelected(city) {
    setSelected({
      id: city.curatedDestinationId || null,
      name: city.name,
      country: city.country,
      countryCode: city.countryCode,
      region: city.region,
      population: city.population,
      latitude: city.latitude,
      longitude: city.longitude,
      hasCuratedItinerary: Boolean(city.curatedDestinationId),
    });
  }

  function toggleListValue(field, value) {
    setPrefs((prev) => {
      const list = prev[field];
      return {
        ...prev,
        [field]: list.includes(value) ? list.filter((v) => v !== value) : [...list, value],
      };
    });
  }

  async function handleGetSuggestions() {
    setLoading(true);
    setError(null);
    try {
      const result = await suggestDestinations({
        budgetLevel: prefs.budgetLevel || null,
        region: prefs.region || null,
        maxFlightDurationHours: prefs.maxFlightDurationHours ? Number(prefs.maxFlightDurationHours) : null,
        vacationStyles: prefs.vacationStyles,
        activities: prefs.activities,
        additionalPreferences: prefs.additionalPreferences,
      });
      setSuggestions(result);
      setPhase("suggestResults");
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  function handleContinue() {
    dispatch({ type: "SET_DESTINATION", destination: selected, departureLocation });
  }

  if (selected) {
    return (
      <div>
        <h2 className="wizard-step__title">Where are you departing from?</h2>
        <p className="wizard-step__subtitle">
          Heading to <strong>{selected.name}, {selected.country}</strong>. Let us know your starting point.
        </p>
        {selected.hasCuratedItinerary === false && (
          <div className="error-banner" style={{ background: "rgba(99,102,241,0.1)", borderColor: "rgba(99,102,241,0.3)", color: "#c7d2fe" }}>
            <Info size={16} /> We don't have curated attractions for {selected.name} yet - you'll still get hotel
            options and a trip schedule, with attraction recommendations coming in a future update.
          </div>
        )}
        <div className="form-group">
          <label>Departure location</label>
          <input
            type="text"
            placeholder="e.g. New York, USA"
            value={departureLocation}
            onChange={(e) => setDepartureLocation(e.target.value)}
          />
        </div>
        <div className="wizard-step__actions">
          <button className="btn btn-secondary" onClick={() => setSelected(null)}>
            <ArrowLeft size={16} /> Choose a different destination
          </button>
          <button className="btn btn-primary" disabled={!departureLocation.trim()} onClick={handleContinue}>
            Continue
          </button>
        </div>
      </div>
    );
  }

  if (phase === "choice") {
    return (
      <div>
        <h2 className="wizard-step__title">Do you already know your destination?</h2>
        <p className="wizard-step__subtitle">We can help you choose if you're not sure yet.</p>
        <div className="option-grid">
          <button className="option-card" onClick={() => setPhase("known")}>
            <span className="option-card__icon">
              <MapPin size={26} />
            </span>
            <span className="option-card__label">Yes, I know where I'm going</span>
          </button>
          <button className="option-card" onClick={() => setPhase("suggestForm")}>
            <span className="option-card__icon">
              <Sparkles size={26} />
            </span>
            <span className="option-card__label">No, suggest a destination</span>
          </button>
        </div>
        <div className="wizard-step__actions">
          <button className="btn btn-secondary" onClick={() => dispatch({ type: "GO_TO", view: "group" })}>
            <ArrowLeft size={16} /> Back
          </button>
          <span />
        </div>
      </div>
    );
  }

  if (phase === "known") {
    return (
      <div>
        <h2 className="wizard-step__title">Choose your destination</h2>
        <p className="wizard-step__subtitle">Search any city worldwide, optionally narrowed down by country.</p>
        <div className="form-group">
          <label>Country <span className="hint">(optional filter)</span></label>
          <select value={countryFilter} onChange={(e) => setCountryFilter(e.target.value)}>
            <option value="">Any country</option>
            {COUNTRIES.map((c) => (
              <option key={c.code} value={c.code}>{c.name}</option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label>City</label>
          <CitySearch countryCode={countryFilter} onSelect={handleCitySelected} />
        </div>
        <div className="wizard-step__actions">
          <button className="btn btn-secondary" onClick={() => setPhase("choice")}>
            <ArrowLeft size={16} /> Back
          </button>
          <span />
        </div>
      </div>
    );
  }

  if (phase === "suggestForm") {
    return (
      <div>
        <h2 className="wizard-step__title">Tell us what you're looking for</h2>
        <p className="wizard-step__subtitle">We'll recommend destinations that match your preferences.</p>
        {error && (
          <div className="error-banner">
            <Info size={16} /> {error}
          </div>
        )}
        <div className="form-row">
          <div className="form-group">
            <label>Budget</label>
            <select value={prefs.budgetLevel} onChange={(e) => setPrefs({ ...prefs, budgetLevel: e.target.value })}>
              <option value="">Any</option>
              {BUDGET_LEVELS.map((b) => (
                <option key={b} value={b}>{label(b)}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Preferred region</label>
            <select value={prefs.region} onChange={(e) => setPrefs({ ...prefs, region: e.target.value })}>
              <option value="">Any</option>
              {REGIONS.map((r) => (
                <option key={r} value={r}>{label(r)}</option>
              ))}
            </select>
          </div>
        </div>
        <div className="form-group">
          <label>Maximum flight duration (hours)</label>
          <input
            type="number"
            min="1"
            placeholder="e.g. 8"
            value={prefs.maxFlightDurationHours}
            onChange={(e) => setPrefs({ ...prefs, maxFlightDurationHours: e.target.value })}
          />
        </div>
        <div className="form-group">
          <label>Vacation style</label>
          <div className="checkbox-grid">
            {VACATION_STYLES.map((style) => (
              <label key={style} className={`checkbox-pill ${prefs.vacationStyles.includes(style) ? "is-checked" : ""}`}>
                <input
                  type="checkbox"
                  checked={prefs.vacationStyles.includes(style)}
                  onChange={() => toggleListValue("vacationStyles", style)}
                />
                {label(style)}
              </label>
            ))}
          </div>
        </div>
        <div className="form-group">
          <label>Activities & entertainment</label>
          <div className="checkbox-grid">
            {ACTIVITIES.map((activity) => (
              <label key={activity} className={`checkbox-pill ${prefs.activities.includes(activity) ? "is-checked" : ""}`}>
                <input
                  type="checkbox"
                  checked={prefs.activities.includes(activity)}
                  onChange={() => toggleListValue("activities", activity)}
                />
                {label(activity)}
              </label>
            ))}
          </div>
        </div>
        <div className="form-group">
          <label>Additional preferences <span className="hint">(optional)</span></label>
          <textarea
            rows={2}
            placeholder="Anything else we should know?"
            value={prefs.additionalPreferences}
            onChange={(e) => setPrefs({ ...prefs, additionalPreferences: e.target.value })}
          />
        </div>
        <div className="wizard-step__actions">
          <button className="btn btn-secondary" onClick={() => setPhase("choice")}>
            <ArrowLeft size={16} /> Back
          </button>
          <button className="btn btn-primary" disabled={loading} onClick={handleGetSuggestions}>
            {loading && <span className="spinner" />}
            Get Suggestions
          </button>
        </div>
      </div>
    );
  }

  // phase === "suggestResults"
  return (
    <div>
      <h2 className="wizard-step__title">Here's what we recommend</h2>
      <p className="wizard-step__subtitle">Pick a destination to continue planning your trip.</p>
      {suggestions.length === 0 ? (
        <p className="wizard-step__subtitle">No destinations matched those preferences. Try loosening your filters.</p>
      ) : (
        <DestinationGrid destinations={suggestions} onSelect={setSelected} showMatchScore />
      )}
      <div className="wizard-step__actions">
        <button className="btn btn-secondary" onClick={() => setPhase("suggestForm")}>
          <ArrowLeft size={16} /> Adjust preferences
        </button>
        <span />
      </div>
    </div>
  );
}
