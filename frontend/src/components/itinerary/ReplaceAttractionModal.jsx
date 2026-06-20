import React, { useEffect, useState } from "react";
import { X, ChevronRight, Info } from "lucide-react";
import { fetchAlternatives, replaceAttraction } from "../../api/api.js";
import { useWizardDispatch } from "../../context/WizardContext.jsx";
import { label } from "../../utils/format.js";

export default function ReplaceAttractionModal({ itineraryId, dayNumber, attraction, onClose }) {
  const dispatch = useWizardDispatch();
  const [alternatives, setAlternatives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchAlternatives(itineraryId, dayNumber, attraction.id)
      .then(setAlternatives)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [itineraryId, dayNumber, attraction.id]);

  async function handlePick(newAttraction) {
    setSubmitting(true);
    try {
      const updated = await replaceAttraction(itineraryId, {
        dayNumber,
        oldAttractionId: attraction.id,
        newAttractionId: newAttraction.id,
      });
      dispatch({ type: "UPDATE_ITINERARY", itinerary: updated });
      onClose();
    } catch (e) {
      setError(e.message);
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal__header">
          <h3>Replace &quot;{attraction.name}&quot;</h3>
          <button className="modal__close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        {error && (
          <div className="error-banner">
            <Info size={16} /> {error}
          </div>
        )}
        {loading ? (
          <p className="wizard-step__subtitle">Loading alternatives...</p>
        ) : alternatives.length === 0 ? (
          <p className="wizard-step__subtitle">No other attractions are available for this trip right now.</p>
        ) : (
          <div className="alternative-list">
            {alternatives.map((alt) => (
              <button key={alt.id} className="alternative-item" disabled={submitting} onClick={() => handlePick(alt)}>
                <span>
                  <strong>{alt.name}</strong>
                  <br />
                  <span className="hint">
                    {label(alt.category)} · {alt.averageVisitDurationMinutes} min
                  </span>
                </span>
                <ChevronRight size={18} />
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
