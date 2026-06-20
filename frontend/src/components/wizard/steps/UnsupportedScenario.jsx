import React from "react";
import { Lock, ArrowLeft, ArrowRight } from "lucide-react";
import { useWizardDispatch } from "../../../context/WizardContext.jsx";

export default function UnsupportedScenario() {
  const dispatch = useWizardDispatch();

  return (
    <div>
      <div className="unsupported-icon">
        <Lock size={30} />
      </div>
      <h2 className="wizard-step__title">Flight & hotel booking isn't available yet</h2>
      <p className="wizard-step__subtitle">
        We're not able to search or book flights and hotels for you just yet - that's a feature we're
        planning for a future version of the app. For now, please book your flights and hotel separately,
        then come back and tell us your dates and hotel details so we can build your itinerary.
      </p>
      <div className="wizard-step__actions">
        <button className="btn btn-secondary" onClick={() => dispatch({ type: "GO_TO", view: "destination" })}>
          <ArrowLeft size={16} /> Change destination
        </button>
        <button className="btn btn-primary" onClick={() => dispatch({ type: "GO_TO", view: "flightHotel" })}>
          I've booked them <ArrowRight size={16} />
        </button>
      </div>
    </div>
  );
}
