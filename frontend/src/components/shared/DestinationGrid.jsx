import React from "react";
import { Globe2 } from "lucide-react";
import { label } from "../../utils/format.js";

export default function DestinationGrid({ destinations, onSelect, showMatchScore = false }) {
  return (
    <div className="destination-grid">
      {destinations.map((destination) => (
        <button key={destination.id} className="destination-card" onClick={() => onSelect(destination)}>
          <div className="destination-card__image">
            <Globe2 size={28} />
          </div>
          <div className="destination-card__body">
            <div className="destination-card__name">
              {destination.name}, {destination.country}
            </div>
            <div className="destination-card__country">
              {label(destination.budgetLevel || "")} budget
              {showMatchScore ? ` · Match score ${destination.matchScore}` : ""}
            </div>
            <div className="destination-card__tags">
              {(destination.vacationStyles || []).slice(0, 3).map((style) => (
                <span key={style} className="tag">
                  {label(style)}
                </span>
              ))}
            </div>
          </div>
        </button>
      ))}
    </div>
  );
}
