import React, { useState } from "react";
import { MapPin, Footprints, Car, Navigation } from "lucide-react";
import AttractionCard from "./AttractionCard.jsx";
import ReplaceAttractionModal from "./ReplaceAttractionModal.jsx";

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
