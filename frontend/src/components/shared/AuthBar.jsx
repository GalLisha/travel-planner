import React, { useState } from "react";
import { LogIn, LogOut, User as UserIcon, ListChecks } from "lucide-react";
import { useWizardState, useWizardDispatch } from "../../context/WizardContext.jsx";
import AuthModal from "./AuthModal.jsx";

export default function AuthBar() {
  const { currentUser } = useWizardState();
  const dispatch = useWizardDispatch();
  const [showModal, setShowModal] = useState(false);

  return (
    <div className="auth-bar">
      {currentUser ? (
        <>
          <span className="auth-bar__user">
            <UserIcon size={14} /> {currentUser.name || currentUser.email}
          </span>
          <button className="auth-bar__link" onClick={() => dispatch({ type: "GO_TO", view: "myTrips" })}>
            <ListChecks size={14} /> My Trips
          </button>
          <button className="auth-bar__link" onClick={() => dispatch({ type: "LOGOUT" })}>
            <LogOut size={14} /> Sign out
          </button>
        </>
      ) : (
        <button className="auth-bar__link" onClick={() => setShowModal(true)}>
          <LogIn size={14} /> Sign in
        </button>
      )}
      {showModal && <AuthModal onClose={() => setShowModal(false)} />}
    </div>
  );
}
