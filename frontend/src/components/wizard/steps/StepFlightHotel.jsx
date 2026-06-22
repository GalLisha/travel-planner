import React, { useEffect, useRef, useState } from "react";
import {
  CheckCircle2,
  PlaneTakeoff,
  ArrowLeft,
  Compass,
  Info,
  PenLine,
  ListChecks,
  Car,
  Bus,
  Loader2,
  AlertCircle,
} from "lucide-react";
import { useWizardDispatch, useWizardState } from "../../../context/WizardContext.jsx";
import { generateItinerary, lookupHotel } from "../../../api/api.js";
import { useDebouncedValue } from "../../../utils/useDebouncedValue.js";
import HotelBrowser from "../../shared/HotelBrowser.jsx";

export default function StepFlightHotel() {
  const dispatch = useWizardDispatch();
  const { destination, travelGroupType, loading, error, arrivalAirport } = useWizardState();
  const [hasBooking, setHasBooking] = useState(null);
  const [phase, setPhase] = useState("dates"); // dates | hotel | transfer
  const [dates, setDates] = useState({ departureDate: "", returnDate: "", arrivalTime: "", departureTime: "" });
  const [hotelMode, setHotelMode] = useState("browse"); // browse | manual
  const [selectedHotel, setSelectedHotel] = useState(null);
  const [manualHotel, setManualHotel] = useState({ hotelName: "", hotelAddress: "", hotelLatitude: null, hotelLongitude: null });
  const [hotelLookup, setHotelLookup] = useState({ status: "idle", message: "", verifiedName: "", matchSource: null });
  const [transferMode, setTransferMode] = useState(null); // TAXI | BUS
  const debouncedHotelName = useDebouncedValue(manualHotel.hotelName, 600);
  const lookupAbortRef = useRef(null);

  useEffect(() => {
    if (hotelMode !== "manual") return;
    const trimmed = debouncedHotelName.trim();
    if (trimmed.length < 3) {
      setHotelLookup({ status: "idle", message: "", verifiedName: "", matchSource: null });
      return;
    }

    if (lookupAbortRef.current) lookupAbortRef.current.abort();
    const controller = new AbortController();
    lookupAbortRef.current = controller;

    setHotelLookup({ status: "checking", message: "", verifiedName: "", matchSource: null });
    lookupHotel(
      { name: trimmed, city: destination.name, country: destination.country, lat: destination.latitude, lon: destination.longitude },
      controller.signal
    )
      .then((result) => {
        if (result.found) {
          setHotelLookup({ status: "found", message: "", verifiedName: trimmed, matchSource: result.matchSource });
          setManualHotel((prev) => ({
            ...prev,
            hotelAddress: result.hotel.address || prev.hotelAddress,
            hotelLatitude: result.hotel.latitude,
            hotelLongitude: result.hotel.longitude,
          }));
        } else {
          setHotelLookup({ status: "not-found", message: result.message, verifiedName: "", matchSource: null });
        }
      })
      .catch((err) => {
        if (err.name === "AbortError") return;
        setHotelLookup({ status: "not-found", message: err.message, verifiedName: "", matchSource: null });
      });

    return () => controller.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedHotelName, hotelMode, destination.name, destination.country, destination.latitude, destination.longitude]);

  function answer(value) {
    setHasBooking(value);
    dispatch({ type: "ANSWER_HAS_BOOKING", value });
  }

  function handleDatesSubmit(e) {
    e.preventDefault();
    setPhase("hotel");
  }

  async function handleBuildItinerary() {
    const hotelName = hotelMode === "browse" ? selectedHotel?.name : manualHotel.hotelName;
    const hotelAddress = hotelMode === "browse" ? selectedHotel?.address : manualHotel.hotelAddress;

    dispatch({ type: "SET_LOADING", loading: true });
    try {
      const itinerary = await generateItinerary({
        destinationId: destination.id,
        travelGroupType,
        departureDate: dates.departureDate,
        returnDate: dates.returnDate,
        hotelName,
        hotelAddress,
        hotelLatitude: hotelMode === "browse" ? selectedHotel?.latitude : manualHotel.hotelLatitude,
        hotelLongitude: hotelMode === "browse" ? selectedHotel?.longitude : manualHotel.hotelLongitude,
        cityName: destination.name,
        countryName: destination.country,
        latitude: destination.latitude,
        longitude: destination.longitude,
        arrivalAirportName: arrivalAirport?.name,
        arrivalAirportLatitude: arrivalAirport?.latitude,
        arrivalAirportLongitude: arrivalAirport?.longitude,
        arrivalTime: dates.arrivalTime,
        departureTime: dates.departureTime,
        transferMode,
      });
      dispatch({ type: "SET_ITINERARY", itinerary });
    } catch (err) {
      dispatch({ type: "SET_ERROR", error: err.message });
    }
  }

  if (hasBooking !== true) {
    return (
      <div>
        <h2 className="wizard-step__title">Do you already have flight tickets and a hotel booked?</h2>
        <p className="wizard-step__subtitle">
          This helps us build your itinerary around your real travel dates and hotel location.
        </p>
        <div className="option-grid">
          <button className="option-card" onClick={() => answer(true)}>
            <span className="option-card__icon">
              <CheckCircle2 size={26} />
            </span>
            <span className="option-card__label">Yes, all booked</span>
          </button>
          <button className="option-card" onClick={() => answer(false)}>
            <span className="option-card__icon">
              <PlaneTakeoff size={26} />
            </span>
            <span className="option-card__label">Not yet</span>
          </button>
        </div>
        <div className="wizard-step__actions">
          <button className="btn btn-secondary" onClick={() => dispatch({ type: "GO_TO", view: "destination" })}>
            <ArrowLeft size={16} /> Back
          </button>
          <span />
        </div>
      </div>
    );
  }

  if (phase === "dates") {
    return (
      <form onSubmit={handleDatesSubmit}>
        <h2 className="wizard-step__title">When are you traveling?</h2>
        <p className="wizard-step__subtitle">We'll use these dates to build your day-by-day itinerary.</p>
        <div className="form-row">
          <div className="form-group">
            <label>Departure date</label>
            <input
              type="date"
              required
              value={dates.departureDate}
              onChange={(e) => setDates({ ...dates, departureDate: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label>Flight lands at</label>
            <input
              type="time"
              required
              value={dates.arrivalTime}
              onChange={(e) => setDates({ ...dates, arrivalTime: e.target.value })}
            />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label>Return date</label>
            <input
              type="date"
              required
              value={dates.returnDate}
              min={dates.departureDate || undefined}
              onChange={(e) => setDates({ ...dates, returnDate: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label>Flight departs at</label>
            <input
              type="time"
              required
              value={dates.departureTime}
              onChange={(e) => setDates({ ...dates, departureTime: e.target.value })}
            />
          </div>
        </div>
        <div className="wizard-step__actions">
          <button type="button" className="btn btn-secondary" onClick={() => setHasBooking(null)}>
            <ArrowLeft size={16} /> Back
          </button>
          <button type="submit" className="btn btn-primary">
            Continue to hotel <Compass size={16} />
          </button>
        </div>
      </form>
    );
  }

  if (phase === "hotel") {
    const canContinueToTransfer =
      hotelMode === "browse"
        ? Boolean(selectedHotel)
        : hotelLookup.status === "found" && hotelLookup.verifiedName === manualHotel.hotelName.trim();

    return (
      <div>
        <h2 className="wizard-step__title">Which hotel are you staying at?</h2>
        <p className="wizard-step__subtitle">
          Pick the hotel you've booked in {destination.name}, or enter it manually if it's not listed.
        </p>
        {error && (
          <div className="error-banner">
            <Info size={16} /> {error}
          </div>
        )}

        <div className="option-grid" style={{ marginBottom: "1.5rem" }}>
          <button
            className={`option-card ${hotelMode === "browse" ? "is-selected" : ""}`}
            type="button"
            onClick={() => setHotelMode("browse")}
          >
            <span className="option-card__icon">
              <ListChecks size={22} />
            </span>
            <span className="option-card__label">Browse hotels</span>
          </button>
          <button
            className={`option-card ${hotelMode === "manual" ? "is-selected" : ""}`}
            type="button"
            onClick={() => setHotelMode("manual")}
          >
            <span className="option-card__icon">
              <PenLine size={22} />
            </span>
            <span className="option-card__label">Enter manually</span>
          </button>
        </div>

        {hotelMode === "browse" ? (
          <HotelBrowser destination={destination} selectedHotelId={selectedHotel?.id} onSelect={setSelectedHotel} />
        ) : (
          <>
            <div className="form-group">
              <label>Hotel name</label>
              <input
                type="text"
                placeholder="e.g. Grand Plaza Hotel"
                value={manualHotel.hotelName}
                onChange={(e) => setManualHotel({ ...manualHotel, hotelName: e.target.value })}
              />
            </div>
            {hotelLookup.status === "checking" && (
              <p className="wizard-step__subtitle">
                <Loader2 size={14} className="spin-icon" /> Checking that this hotel exists...
              </p>
            )}
            {hotelLookup.status === "found" &&
              hotelLookup.verifiedName === manualHotel.hotelName.trim() &&
              hotelLookup.matchSource === "AI" && (
                <p className="wizard-step__subtitle">
                  <AlertCircle size={14} /> Found via AI &mdash; please double-check this address, it isn't independently verified.
                </p>
              )}
            {hotelLookup.status === "found" &&
              hotelLookup.verifiedName === manualHotel.hotelName.trim() &&
              hotelLookup.matchSource !== "AI" && (
                <p className="wizard-step__subtitle">
                  <CheckCircle2 size={14} /> Found it near {destination.name} &mdash; address filled in below.
                </p>
              )}
            {hotelLookup.status === "not-found" && (
              <div className="error-banner">
                <AlertCircle size={16} /> {hotelLookup.message}
              </div>
            )}
            <div className="form-group">
              <label>Hotel address</label>
              <input
                type="text"
                placeholder="Street, city"
                value={manualHotel.hotelAddress}
                onChange={(e) => setManualHotel({ ...manualHotel, hotelAddress: e.target.value })}
              />
            </div>
          </>
        )}

        <div className="wizard-step__actions">
          <button type="button" className="btn btn-secondary" onClick={() => setPhase("dates")}>
            <ArrowLeft size={16} /> Back
          </button>
          <button type="button" className="btn btn-primary" disabled={!canContinueToTransfer} onClick={() => setPhase("transfer")}>
            Continue <Compass size={16} />
          </button>
        </div>
      </div>
    );
  }

  // phase === "transfer"
  return (
    <div>
      <h2 className="wizard-step__title">How will you get from the airport to your hotel?</h2>
      <p className="wizard-step__subtitle">
        We'll calculate the transfer time and add it to the start and end of your itinerary.
      </p>
      {error && (
        <div className="error-banner">
          <Info size={16} /> {error}
        </div>
      )}
      <div className="option-grid">
        <button
          className={`option-card ${transferMode === "TAXI" ? "is-selected" : ""}`}
          type="button"
          onClick={() => setTransferMode("TAXI")}
        >
          <span className="option-card__icon">
            <Car size={26} />
          </span>
          <span className="option-card__label">Taxi</span>
        </button>
        <button
          className={`option-card ${transferMode === "BUS" ? "is-selected" : ""}`}
          type="button"
          onClick={() => setTransferMode("BUS")}
        >
          <span className="option-card__icon">
            <Bus size={26} />
          </span>
          <span className="option-card__label">Bus</span>
        </button>
      </div>
      <div className="wizard-step__actions">
        <button type="button" className="btn btn-secondary" onClick={() => setPhase("hotel")}>
          <ArrowLeft size={16} /> Back
        </button>
        <button type="button" className="btn btn-primary" disabled={loading || !transferMode} onClick={handleBuildItinerary}>
          {loading ? <span className="spinner" /> : <Compass size={18} />}
          Build My Itinerary
        </button>
      </div>
    </div>
  );
}
