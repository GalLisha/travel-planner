import React from "react";
import { Clock, DollarSign, RefreshCw } from "lucide-react";
import { label } from "../../utils/format.js";

export default function AttractionCard({ item, onReplace }) {
  const { attraction } = item;
  return (
    <div className="attraction-card">
      <div className="attraction-card__time">
        {item.arrivalTime}
        <small>{item.visitDurationMinutes} min visit</small>
      </div>
      <div className="attraction-card__body">
        <div className="attraction-card__head">
          <div>
            <div className="attraction-card__name">{attraction.name}</div>
            <p className="attraction-card__desc">{attraction.description}</p>
          </div>
          <button className="btn-replace" onClick={onReplace}>
            <RefreshCw size={13} /> Replace
          </button>
        </div>
        <div className="attraction-card__meta">
          <span className="tag">{label(attraction.category)}</span>
          <span>
            <Clock size={13} /> {attraction.openingHours}
          </span>
          <span>
            <DollarSign size={13} /> {label(attraction.estimatedCost)}
          </span>
        </div>
      </div>
    </div>
  );
}
