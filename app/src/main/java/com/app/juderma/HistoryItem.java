package com.app.juderma;

import android.net.Uri;

public class HistoryItem {
    private String timestamp;
    private String prediction;
    private String description;
    private Uri imageUri;

    // Constructor
    public HistoryItem(String timestamp, String prediction, String description, Uri imageUri) {
        this.timestamp = timestamp;
        this.prediction = prediction;
        this.description = description;
        this.imageUri = imageUri;
    }

    // Getters
    public String getTimestamp() {
        return timestamp;
    }

    public String getPrediction() {
        return prediction;
    }

    public String getDescription() {
        return description;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    // Setters
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}