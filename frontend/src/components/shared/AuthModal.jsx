import React, { useState } from "react";
import { X, Info } from "lucide-react";
import { signIn, signUp } from "../../api/api.js";
import { useWizardDispatch } from "../../context/WizardContext.jsx";

export default function AuthModal({ onClose, onSuccess, defaultMode = "signin" }) {
  const dispatch = useWizardDispatch();
  const [mode, setMode] = useState(defaultMode); // signin | signup
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const result = mode === "signup" ? await signUp(email, password, name) : await signIn(email, password);
      dispatch({ type: "SET_USER", user: result.user, token: result.token });
      onSuccess?.(result);
      onClose();
    } catch (err) {
      setError(err.message);
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal__header">
          <h3>{mode === "signup" ? "Create an account" : "Sign in"}</h3>
          <button className="modal__close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        {error && (
          <div className="error-banner">
            <Info size={16} /> {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          {mode === "signup" && (
            <div className="form-group">
              <label>Name <span className="hint">(optional)</span></label>
              <input type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="Jane Doe" />
            </div>
          )}
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="At least 6 characters"
            />
          </div>
          <div className="wizard-step__actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setMode(mode === "signup" ? "signin" : "signup")}
            >
              {mode === "signup" ? "Have an account? Sign in" : "New here? Sign up"}
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting && <span className="spinner" />}
              {mode === "signup" ? "Sign Up" : "Sign In"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
