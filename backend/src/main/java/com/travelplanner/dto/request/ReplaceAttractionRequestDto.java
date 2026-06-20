package com.travelplanner.dto.request;

public class ReplaceAttractionRequestDto {
    private int dayNumber;
    private String oldAttractionId;
    private String newAttractionId;

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getOldAttractionId() {
        return oldAttractionId;
    }

    public void setOldAttractionId(String oldAttractionId) {
        this.oldAttractionId = oldAttractionId;
    }

    public String getNewAttractionId() {
        return newAttractionId;
    }

    public void setNewAttractionId(String newAttractionId) {
        this.newAttractionId = newAttractionId;
    }
}
