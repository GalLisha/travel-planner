import React, { useEffect, useState } from "react";
import { Calendar, MapPin, AlertCircle, RotateCw, ArrowLeft } from "lucide-react";
import { useWizardState, useWizardDispatch } from "../../context/WizardContext.jsx";
import { listMyTrips } from "../../api/api.js";

export default function MyTripsView() {
  const { authToken } = useWizardState();
  const dispatch = useWizardDispatch();
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadToken, setReloadToken] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    listMyTrips(authToken)
      .then((result) => {
        if (active) setTrips(result);
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
  }, [authToken, reloadToken]);

  function openTrip(trip) {
    dispatch({
      type: "SET_ITINERARY",
      itinerary: trip.itinerary,
      destination: { name: trip.destinationName, country: trip.countryName },
    });
  }

  return (
    <div className="itinerary-view">
      <div className="itinerary-header">
        <div>
          <h1 className="itinerary-header__title">My Trips</h1>
          <p className="itinerary-header__meta">Your previously saved itineraries.</p>
        </div>
        <button className="btn btn-secondary" onClick={() => dispatch({ type: "GO_TO", view: "landing" })}>
          <ArrowLeft size={16} /> Back
        </button>
      </div>

      {loading ? (
        <div className="hotel-browser__status">
          <span className="spinner" /> Loading your trips...
        </div>
      ) : error ? (
        <div className="error-banner">
          <AlertCircle size={16} /> {error}
          <button type="button" className="btn-replace" onClick={() => setReloadToken((t) => t + 1)}>
            <RotateCw size={13} /> Retry
          </button>
        </div>
      ) : trips.length === 0 ? (
        <p className="wizard-step__subtitle">You haven&apos;t saved any trips yet.</p>
      ) : (
        <div className="hotel-grid">
          {trips.map((trip) => (
            <button key={trip.id} className="hotel-card airport-card" onClick={() => openTrip(trip)}>
              <div className="hotel-card__body">
                <div className="hotel-card__name">
                  {trip.destinationName}, {trip.countryName}
                </div>
                <div className="hotel-card__meta">
                  <span>
                    <Calendar size={13} /> {trip.departureDate} &ndash; {trip.returnDate}
                  </span>
                  <span>
                    <MapPin size={13} /> Saved {new Date(trip.savedAt).toLocaleDateString()}
                  </span>
                </div>
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
