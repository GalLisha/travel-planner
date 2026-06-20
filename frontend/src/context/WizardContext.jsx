import React, { createContext, useContext, useReducer } from "react";

const AUTH_STORAGE_KEY = "vacationPlannerAuth";

function loadStoredAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    return raw ? JSON.parse(raw) : { currentUser: null, authToken: null };
  } catch {
    return { currentUser: null, authToken: null };
  }
}

const initialState = {
  view: "landing", // landing | group | destination | flightHotel | unsupported | itinerary | myTrips
  travelGroupType: null, // FAMILY | COUPLE | FRIENDS
  destination: null, // DestinationDto
  arrivalAirport: null, // { name, iataCode, latitude, longitude } | { name } for manual entry
  hasFlightsAndHotel: null,
  itinerary: null,
  loading: false,
  error: null,
  ...loadStoredAuth(), // currentUser, authToken - persisted across reloads
};

function reducer(state, action) {
  switch (action.type) {
    case "START_PLANNING":
      return { ...initialState, currentUser: state.currentUser, authToken: state.authToken, view: "group" };
    case "SET_GROUP_TYPE":
      return { ...state, travelGroupType: action.travelGroupType, view: "destination" };
    case "SET_DESTINATION":
      return {
        ...state,
        destination: action.destination,
        arrivalAirport: action.arrivalAirport,
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
      return {
        ...state,
        itinerary: action.itinerary,
        destination: action.destination || state.destination,
        view: "itinerary",
        loading: false,
        error: null,
      };
    case "UPDATE_ITINERARY":
      return { ...state, itinerary: action.itinerary, error: null };
    case "GO_TO":
      return { ...state, view: action.view, error: null };
    case "SET_USER":
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({ currentUser: action.user, authToken: action.token }));
      return { ...state, currentUser: action.user, authToken: action.token };
    case "LOGOUT":
      localStorage.removeItem(AUTH_STORAGE_KEY);
      return { ...state, currentUser: null, authToken: null };
    case "RESET":
      return { ...initialState, currentUser: state.currentUser, authToken: state.authToken };
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
