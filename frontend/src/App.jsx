import React from "react";
import { WizardProvider, useWizardState } from "./context/WizardContext.jsx";
import LandingPage from "./components/landing/LandingPage.jsx";
import StepTravelGroup from "./components/wizard/steps/StepTravelGroup.jsx";
import StepDestination from "./components/wizard/steps/StepDestination.jsx";
import StepFlightHotel from "./components/wizard/steps/StepFlightHotel.jsx";
import UnsupportedScenario from "./components/wizard/steps/UnsupportedScenario.jsx";
import ItineraryView from "./components/itinerary/ItineraryView.jsx";
import WizardLayout from "./components/wizard/WizardLayout.jsx";

function Screens() {
  const { view } = useWizardState();

  if (view === "landing") {
    return <LandingPage />;
  }

  if (view === "itinerary") {
    return <ItineraryView />;
  }

  const steps = ["group", "destination", "flightHotel", "unsupported"];
  const stepIndex = steps.indexOf(view);

  return (
    <WizardLayout stepIndex={stepIndex} totalSteps={3}>
      {view === "group" && <StepTravelGroup />}
      {view === "destination" && <StepDestination />}
      {view === "flightHotel" && <StepFlightHotel />}
      {view === "unsupported" && <UnsupportedScenario />}
    </WizardLayout>
  );
}

export default function App() {
  return (
    <WizardProvider>
      <Screens />
    </WizardProvider>
  );
}
