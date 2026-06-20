import React, { createContext, useContext, useReducer } from "react";

const initialState = {
  view: "landing", // landing | group | destination | flightHotel | unsupported | itinerary
  travelGroupType: null, // FAMILY | COUPLE | FRIENDS
  destination: null, // DestinationDto
  departureLocation: "",
  hasFlightsAndHotel: null,
  itinerary: null,
  loading: false,
  error: null,
};

function reducer(state, action) {
  switch (action.type) {
    case "START_PLANNING":
      return { ...initialState, view: "group" };
    case "SET_GROUP_TYPE":
      return { ...state, travelGroupType: action.travelGroupType, view: "destination" };
    case "SET_DESTINATION":
      return {
        ...state,
        destination: action.destination,
        departureLocation: action.departureLocation,
        view: "flightHotel",
      };
    case "ANSWER_HAS_BOOKING":
      return {
        ...state,
        hasFlightsAndHotel: action.value,
        view: action.value ? "flightHotel" : "unsupported",
      };
    case "SET_LOADING":
      return { ...state, loading: action.loading };
    case "SET_ERROR":
      return { ...state, error: action.error, loading: false };
    case "SET_ITINERARY":
      return { ...state, itinerary: action.itinerary, view: "itinerary", loading: false, error: null };
    case "UPDATE_ITINERARY":
      return { ...state, itinerary: action.itinerary, error: null };
    case "GO_TO":
      return { ...state, view: action.view, error: null };
    case "RESET":
      return { ...initialState };
    default:
      return state;
  }
}

const WizardStateContext = createContext(null);
const WizardDispatchContext = createContext(null);

export function WizardProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, initialState);
  return (
    <WizardStateContext.Provider value={state}>
      <WizardDispatchContext.Provider value={dispatch}>{children}</WizardDispatchContext.Provider>
    </WizardStateContext.Provider>
  );
}

export function useWizardState() {
  const ctx = useContext(WizardStateContext);
  if (!ctx) throw new Error("useWizardState must be used within a WizardProvider");
  return ctx;
}

export function useWizardDispatch() {
  const ctx = useContext(WizardDispatchContext);
  if (!ctx) throw new Error("useWizardDispatch must be used within a WizardProvider");
  return ctx;
}
