const BASE_URL = `${import.meta.env.VITE_API_BASE_URL || ""}/api`;

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...options.headers },
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body = await response.json();
      message = body.message || message;
    } catch {
      // response had no JSON body
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export function fetchDestinations() {
  return request("/destinations");
}

export function suggestDestinations(preferences) {
  return request("/destinations/suggestions", {
    method: "POST",
    body: JSON.stringify(preferences),
  });
}

export function fetchAttractions(destinationId) {
  return request(`/attractions?destinationId=${encodeURIComponent(destinationId)}`);
}

export function generateItinerary(payload) {
  return request("/itinerary", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function fetchAlternatives(itineraryId, dayNumber, attractionId) {
  return request(
    `/itinerary/${itineraryId}/alternatives?dayNumber=${dayNumber}&attractionId=${encodeURIComponent(attractionId)}`
  );
}

export function replaceAttraction(itineraryId, payload) {
  return request(`/itinerary/${itineraryId}/replace`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function searchCities(query, countryCode, signal) {
  const params = new URLSearchParams({ query });
  if (countryCode) params.set("countryCode", countryCode);
  return request(`/cities/search?${params.toString()}`, { signal });
}

export function searchHotels({ city, country, lat, lon }, signal) {
  const params = new URLSearchParams({ city, country: country || "", lat, lon });
  return request(`/hotels/search?${params.toString()}`, { signal });
}

export function searchAirports(lat, lon, signal) {
  const params = new URLSearchParams({ lat, lon });
  return request(`/airports/search?${params.toString()}`, { signal });
}

export function signUp(email, password, name) {
  return request("/auth/signup", {
    method: "POST",
    body: JSON.stringify({ email, password, name }),
  });
}

export function signIn(email, password) {
  return request("/auth/signin", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function saveTrip(token, payload) {
  return request("/trips", {
    method: "POST",
    body: JSON.stringify(payload),
    headers: { Authorization: `Bearer ${token}` },
  });
}

export function listMyTrips(token) {
  return request("/trips", {
    headers: { Authorization: `Bearer ${token}` },
  });
}
