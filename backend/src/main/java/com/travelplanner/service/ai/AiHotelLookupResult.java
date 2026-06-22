package com.travelplanner.service.ai;

/** The AI's plausibility assessment of a manually-typed hotel name, used as the last
 * step of the hotel-validation waterfall when no real provider can confirm a match. */
public class AiHotelLookupResult {
    public boolean plausible;
    public String approximateAddress;
}
