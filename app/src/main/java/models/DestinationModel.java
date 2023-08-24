package models;

public class DestinationModel {
    private String destinationName;
    private String description;

    public DestinationModel() {
        // Default constructor required for Firestore
    }

    public DestinationModel(String destinationName, String description) {
        this.destinationName = destinationName;
        this.description = description;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getDescription() {
        return description;
    }
}
