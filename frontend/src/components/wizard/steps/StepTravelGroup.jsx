import React, { useState } from "react";
import { Smile, Users, Heart, PartyPopper, ArrowLeft } from "lucide-react";
import { useWizardDispatch } from "../../../context/WizardContext.jsx";

export default function StepTravelGroup() {
  const dispatch = useWizardDispatch();
  const [phase, setPhase] = useState("children"); // "children" | "subtype"

  const chooseFamily = () => dispatch({ type: "SET_GROUP_TYPE", travelGroupType: "FAMILY" });
  const chooseSubtype = (type) => dispatch({ type: "SET_GROUP_TYPE", travelGroupType: type });

  if (phase === "subtype") {
    return (
      <div>
        <h2 className="wizard-step__title">What kind of trip is this?</h2>
        <p className="wizard-step__subtitle">We'll tailor recommendations to match the vibe.</p>
        <div className="option-grid">
          <button className="option-card" onClick={() => chooseSubtype("COUPLE")}>
            <span className="option-card__icon">
              <Heart size={26} />
            </span>
            <span className="option-card__label">Couple trip</span>
            <span className="option-card__hint">Romantic spots, fine dining, scenic moments</span>
          </button>
          <button className="option-card" onClick={() => chooseSubtype("FRIENDS")}>
            <span className="option-card__icon">
              <PartyPopper size={26} />
            </span>
            <span className="option-card__label">Friends trip</span>
            <span className="option-card__hint">Nightlife, adventure, group entertainment</span>
          </button>
        </div>
        <div className="wizard-step__actions">
          <button className="btn btn-secondary" onClick={() => setPhase("children")}>
            <ArrowLeft size={16} /> Back
          </button>
          <span />
        </div>
      </div>
    );
  }

  return (
    <div>
      <h2 className="wizard-step__title">Are you traveling with children?</h2>
      <p className="wizard-step__subtitle">This helps us recommend the right kind of attractions.</p>
      <div className="option-grid">
        <button className="option-card" onClick={chooseFamily}>
          <span className="option-card__icon">
            <Smile size={26} />
          </span>
          <span className="option-card__label">Yes, with children</span>
          <span className="option-card__hint">Family-friendly attractions & activities</span>
        </button>
        <button className="option-card" onClick={() => setPhase("subtype")}>
          <span className="option-card__icon">
            <Users size={26} />
          </span>
          <span className="option-card__label">No</span>
          <span className="option-card__hint">Couple or friends trip</span>
        </button>
      </div>
    </div>
  );
}
