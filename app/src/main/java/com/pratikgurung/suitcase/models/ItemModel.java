package com.pratikgurung.suitcase.models;

public class ItemModel {
    private String itemName, itemDescription, itemPrice, itemImage, destinationDocumentId;
    private boolean purchased;

    public ItemModel() {
        // Default constructor required for calls to DataSnapshot.getValue(ItemModel.class)
    }

    public ItemModel(String itemName, String itemDescription, String itemPrice, String itemImage, String destinationDocumentId) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;
        this.destinationDocumentId = destinationDocumentId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getDestinationDocumentId() {
        return destinationDocumentId;
    }

    public void setDestinationDocumentId(String destinationDocumentId) {
        this.destinationDocumentId = destinationDocumentId;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
}
