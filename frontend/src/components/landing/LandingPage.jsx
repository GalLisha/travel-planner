import React from "react";
import { Compass, ArrowRight, Users, Map, RefreshCw } from "lucide-react";
import { useWizardDispatch } from "../../context/WizardContext.jsx";

export default function LandingPage() {
  const dispatch = useWizardDispatch();

  return (
    <div className="landing-page">
      <span className="landing-page__badge">
        <Compass size={14} /> AI-assisted trip planning
      </span>
      <h1 className="landing-page__title">
        Your next vacation, <span>planned in minutes</span>
      </h1>
      <p className="landing-page__subtitle">
        Tell us who you're traveling with and what you love, and we'll build a day-by-day
        itinerary with optimized routes, travel times and attractions picked just for your trip.
      </p>
      <button className="landing-page__cta" onClick={() => dispatch({ type: "START_PLANNING" })}>
        Plan My Vacation <ArrowRight size={20} />
      </button>
      <div className="landing-page__features">
        <div className="landing-page__feature">
          <Users size={18} /> Tailored for family, couple or friends trips
        </div>
        <div className="landing-page__feature">
          <Map size={18} /> Optimized daily routes & travel times
        </div>
        <div className="landing-page__feature">
          <RefreshCw size={18} /> Swap any attraction, instantly recalculated
        </div>
      </div>
    </div>
  );
}
