package com.saferoute.alert_service.dto;

public class TelemetryDTO {
    private String truckId;
    private Double temperature;
    private Double latitude;
    private Double longitude;
    private String timestamp;

    public TelemetryDTO() {
    }

    public TelemetryDTO(String truckId, Double temperature, Double latitude, Double longitude, String timestamp) {
        this.truckId = truckId;
        this.temperature = temperature;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getTruckId() {
        return truckId;
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isValid() {
        return truckId != null && !truckId.isEmpty() && temperature != null;
    }
}
