import React, { useState } from "react";
import { Calendar, Hotel, RotateCcw, Save, CheckCircle2, AlertCircle } from "lucide-react";
import { useWizardState, useWizardDispatch } from "../../context/WizardContext.jsx";
import { saveTrip } from "../../api/api.js";
import AuthModal from "../shared/AuthModal.jsx";
import DayCard from "./DayCard.jsx";

export default function ItineraryView() {
  const { itinerary, destination, authToken } = useWizardState();
  const dispatch = useWizardDispatch();
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [saveState, setSaveState] = useState("idle"); // idle | saving | saved | error
  const [saveError, setSaveError] = useState(null);

  if (!itinerary) return null;

  async function doSave(token) {
    setSaveState("saving");
    setSaveError(null);
    try {
      await saveTrip(token, {
        destinationName: destination?.name,
        countryName: destination?.country,
        departureDate: itinerary.departureDate,
        returnDate: itinerary.returnDate,
        itinerary,
      });
      setSaveState("saved");
    } catch (err) {
      setSaveError(err.message);
      setSaveState("error");
    }
  }

  function handleSaveClick() {
    if (!authToken) {
      setShowAuthModal(true);
      return;
    }
    doSave(authToken);
  }

  return (
    <div className="itinerary-view">
      <div className="itinerary-header">
        <div>
          <h1 className="itinerary-header__title">Your trip to {destination?.name}</h1>
          <p className="itinerary-header__meta">
            <Calendar size={15} />
            <strong>{itinerary.departureDate}</strong> &ndash; <strong>{itinerary.returnDate}</strong>
            {itinerary.hotelName && (
              <>
                <span>·</span>
                <Hotel size={15} />
                Staying at <strong>{itinerary.hotelName}</strong>
              </>
            )}
          </p>
          {saveState === "error" && (
            <div className="error-banner" style={{ marginTop: "0.75rem" }}>
              <AlertCircle size={16} /> {saveError}
            </div>
          )}
        </div>
        <div style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap" }}>
          <button
            className="btn btn-secondary"
            disabled={saveState === "saving" || saveState === "saved"}
            onClick={handleSaveClick}
          >
            {saveState === "saved" ? (
              <>
                <CheckCircle2 size={16} /> Saved
              </>
            ) : (
              <>
                {saveState === "saving" ? <span className="spinner" /> : <Save size={16} />}
                Save Trip
              </>
            )}
          </button>
          <button className="btn btn-secondary" onClick={() => dispatch({ type: "RESET" })}>
            <RotateCcw size={16} /> Plan another vacation
          </button>
        </div>
      </div>

      {itinerary.days.map((day) => (
        <DayCard key={day.dayNumber} day={day} itineraryId={itinerary.id} />
      ))}

      {showAuthModal && (
        <AuthModal onClose={() => setShowAuthModal(false)} onSuccess={(result) => doSave(result.token)} />
      )}
    </div>
  );
}
