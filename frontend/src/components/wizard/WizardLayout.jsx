import React from "react";
import { Check } from "lucide-react";

export default function WizardLayout({ stepIndex, totalSteps, children }) {
  const clampedIndex = stepIndex < 0 ? 0 : stepIndex;
  const fillPercent = totalSteps > 1 ? Math.min(100, (clampedIndex / (totalSteps - 1)) * 100) : 0;

  return (
    <div className="wizard-layout">
      <div className="wizard-progress">
        <div className="wizard-progress__fill" style={{ width: `${fillPercent}%` }} />
        {Array.from({ length: totalSteps }, (_, i) => {
          const state = i < clampedIndex ? "is-complete" : i === clampedIndex ? "is-active" : "";
          return (
            <div key={i} className={`wizard-progress__step ${state}`}>
              {i < clampedIndex ? <Check size={14} /> : i + 1}
            </div>
          );
        })}
      </div>
      <div className="wizard-card">{children}</div>
    </div>
  );
}
