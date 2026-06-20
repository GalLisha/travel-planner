import React, { useState } from "react";
import { MapPin, Footprints, Car, Bus, Navigation } from "lucide-react";
import AttractionCard from "./AttractionCard.jsx";
import ReplaceAttractionModal from "./ReplaceAttractionModal.jsx";

function TransferLegRow({ leg }) {
  const Icon = leg.mode === "BUS" ? Bus : Car;
  return (
    <div className="transfer-leg">
      <div className="transfer-leg__icon">
        <Icon size={16} />
      </div>
      <div className="transfer-leg__body">
        <div className="transfer-leg__route">
          {leg.fromLabel} &rarr; {leg.toLabel}
        </div>
        <div className="transfer-leg__meta">
          <span>{leg.departureTime} &ndash; {leg.arrivalTime}</span>
          <span>{leg.travelTimeMinutes} min &middot; {leg.distanceKm} km &middot; {leg.mode === "BUS" ? "Bus" : "Taxi"}</span>
        </div>
      </div>
    </div>
  );
}

export default function DayCard({ day, itineraryId }) {
  const [replacing, setReplacing] = useState(null);

  return (
    <div className="day-card">
      <div className="day-card__header">
        <div>
          <div className="day-card__title">Day {day.dayNumber}</div>
          <div className="day-card__date">{day.date}</div>
        </div>
        <div className="day-card__summary">
          {day.items.length > 0 ? (
            <span className="tag">
              <Navigation size={11} /> {day.totalDistanceKm} km · {day.totalTravelTimeMinutes} min travel
            </span>
          ) : (
            <span className="tag">Free day</span>
          )}
        </div>
      </div>

      {day.arrivalTransfer && <TransferLegRow leg={day.arrivalTransfer} />}

      {day.items.length === 0 ? (
        <p className="day-card__empty">
          No attractions scheduled - enjoy a free day to relax or explore on your own.
        </p>
      ) : (
        <div className="timeline">
          {day.items.map((item, idx) => (
            <React.Fragment key={item.attraction.id}>
              {idx > 0 && (
                <div className="travel-connector">
                  {item.travelMode === "WALK" ? <Footprints size={14} /> : <Car size={14} />}
                  {item.travelTimeFromPreviousMinutes} min · {item.travelDistanceFromPreviousKm} km
                </div>
              )}
              <div className="timeline-item">
                <div className="timeline-item__dot">
                  <MapPin size={11} />
                </div>
                <AttractionCard item={item} onReplace={() => setReplacing(item.attraction)} />
              </div>
            </React.Fragment>
          ))}
        </div>
      )}

      {day.departureTransfer && <TransferLegRow leg={day.departureTransfer} />}

      {replacing && (
        <ReplaceAttractionModal
          itineraryId={itineraryId}
          dayNumber={day.dayNumber}
          attraction={replacing}
          onClose={() => setReplacing(null)}
        />
      )}
    </div>
  );
}
