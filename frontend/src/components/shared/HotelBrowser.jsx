import React, { useEffect, useState } from "react";
import { Star, MapPinned, Building2, AlertCircle, RotateCw, ChevronLeft, ChevronRight } from "lucide-react";
import { searchHotels } from "../../api/api.js";

const HOTELS_PER_PAGE = 10;

function StarRating({ rating }) {
  if (!rating) return null;
  const full = Math.round(rating);
  return (
    <span className="hotel-card__stars" title={`${rating} / 5`}>
      {Array.from({ length: 5 }, (_, i) => (
        <Star key={i} size={13} fill={i < full ? "currentColor" : "none"} />
      ))}
    </span>
  );
}

export default function HotelBrowser({ destination, selectedHotelId, onSelect }) {
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadToken, setReloadToken] = useState(0);
  const [page, setPage] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    setPage(0);
    searchHotels({
      city: destination.name,
      country: destination.country,
      lat: destination.latitude,
      lon: destination.longitude,
    })
      .then((result) => {
        if (active) setHotels(result);
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
  }, [destination.name, destination.country, destination.latitude, destination.longitude, reloadToken]);

  if (loading) {
    return (
      <div className="hotel-browser__status">
        <span className="spinner" /> Finding hotels in {destination.name}...
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

  if (hotels.length === 0) {
    return <p className="wizard-step__subtitle">No hotels found in {destination.name} right now.</p>;
  }

  const pageCount = Math.ceil(hotels.length / HOTELS_PER_PAGE);
  const pagedHotels = hotels.slice(page * HOTELS_PER_PAGE, (page + 1) * HOTELS_PER_PAGE);

  return (
    <div>
      <div className="hotel-grid">
        {pagedHotels.map((hotel) => (
          <button
            key={hotel.id}
            className={`hotel-card ${selectedHotelId === hotel.id ? "is-selected" : ""}`}
            onClick={() => onSelect(hotel)}
          >
            <div className="hotel-card__image">
              {hotel.imageUrl ? <img src={hotel.imageUrl} alt={hotel.name} /> : <Building2 size={26} />}
            </div>
            <div className="hotel-card__body">
              <div className="hotel-card__name">{hotel.name}</div>
              <StarRating rating={hotel.starRating} />
              <div className="hotel-card__meta">
                {hotel.minPricePerNight ? (
                  <span className="tag">
                    ${hotel.minPricePerNight}&ndash;${hotel.maxPricePerNight}/night
                  </span>
                ) : null}
                <span>
                  <MapPinned size={13} /> {hotel.distanceFromCenterKm} km from center
                </span>
              </div>
            </div>
          </button>
        ))}
      </div>
      {pageCount > 1 && (
        <div className="hotel-pagination">
          <button
            type="button"
            className="btn btn-secondary"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            <ChevronLeft size={16} /> Prev
          </button>
          <span className="hotel-pagination__status">
            Page {page + 1} of {pageCount}
          </span>
          <button
            type="button"
            className="btn btn-secondary"
            disabled={page >= pageCount - 1}
            onClick={() => setPage((p) => Math.min(pageCount - 1, p + 1))}
          >
            Next <ChevronRight size={16} />
          </button>
        </div>
      )}
    </div>
  );
}
