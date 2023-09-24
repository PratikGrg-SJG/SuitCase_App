package models;

public class DestinationModel {
    private String destinationName;
    private String notes;
    private String selectedDate;

    public DestinationModel() {
        // Default constructor required for Firestore
    }

    public DestinationModel(String destinationName, String notes, String selectedDate) {
        this.destinationName = destinationName;
        this.notes = notes;
        this.selectedDate = selectedDate;
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
}

