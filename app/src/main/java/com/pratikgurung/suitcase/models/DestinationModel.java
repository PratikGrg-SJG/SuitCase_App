package com.pratikgurung.suitcase.models;

import java.util.List;

public class DestinationModel {

    private String destinationName;
    private String notes;
    private String selectedDate;
    private String documentId;
    private String userId;
    private List<ItemModel> items;

    public DestinationModel() {
        // Default constructor required for calls to DataSnapshot.getValue(DestinationModel.class)
    }


    public DestinationModel(String destinationName, String notes, String selectedDate, String userId) {
        this.destinationName = destinationName;
        this.notes = notes;
        this.selectedDate = selectedDate;
        this.userId = userId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}

