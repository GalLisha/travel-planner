import React from "react";
import { Calendar, Hotel, RotateCcw } from "lucide-react";
import { useWizardState, useWizardDispatch } from "../../context/WizardContext.jsx";
import DayCard from "./DayCard.jsx";

export default function ItineraryView() {
  const { itinerary, destination } = useWizardState();
  const dispatch = useWizardDispatch();

  if (!itinerary) return null;

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
        </div>
        <button className="btn btn-secondary" onClick={() => dispatch({ type: "RESET" })}>
          <RotateCcw size={16} /> Plan another vacation
        </button>
      </div>

      {itinerary.days.map((day) => (
        <DayCard key={day.dayNumber} day={day} itineraryId={itinerary.id} />
      ))}
    </div>
  );
}
