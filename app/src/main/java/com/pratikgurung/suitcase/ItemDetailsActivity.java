package com.pratikgurung.suitcase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class ItemDetailsActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    ImageView itemImageView, imageDialog;
    TextView itemNameView;
    TextView itemPriceView;
    TextView itemDescriptionView;
    MaterialButton shareButton;
    LinearLayout editItemID, deleteItemID;
    String itemDocumentIdP, itemName, itemPrice, itemDescription, itemImageUrl;
    private static final int REQUEST_CODE_GALLERY = 1001;

    private FirebaseFirestore firestore;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private Dialog editDialog;
    private boolean dialogShown = false;  // Flag to track if dialog has been shown for shake gesture






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbarItemsDetail);
        itemImageView = findViewById(R.id.imageViewItemDetail);
        itemNameView = findViewById(R.id.textViewItemDetailName);
        itemPriceView = findViewById(R.id.textViewItemDetailPrice);
        itemDescriptionView = findViewById(R.id.textViewItemDetailDescription);
        editItemID = findViewById(R.id.editItemID);
        deleteItemID = findViewById(R.id.deleteItemID);
        shareButton = findViewById(R.id.bottomShareButton);


        // Retrieve data from intent
        Intent intent = getIntent();
         itemDocumentIdP = intent.getStringExtra("itemDocumentId");

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Item Details"); //setting appbar title text
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //enable back arrow button

        fetchItemDetails(itemDocumentIdP);

        //for shake gesture
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration = 10f;  // You can adjust the sensitivity as needed
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;



        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the ShareActivity
                Intent intent = new Intent(ItemDetailsActivity.this, ShareActivity.class);

                // Pass the item image URL and share message as extras to the intent
                intent.putExtra("itemImageURL", itemImageUrl);  // Use the variable directly
                intent.putExtra("shareMessage", constructShareMessage());

                startActivity(intent);
            }
        });

        editItemID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        deleteItemID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteItem(itemDocumentIdP);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed for this example
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (editDialog != null && editDialog.isShowing()) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // Capture accelerometer data
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    lastAcceleration = currentAcceleration;
                    currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
                    float delta = currentAcceleration - lastAcceleration;
                    acceleration = acceleration * 0.9f + delta;

                    // If the acceleration is above a certain threshold (shake detected), clear text fields
                    if (acceleration > 15) {
                        // Shake detected, clear text fields
                        clearTextFields();
                    }
                }
            }
        }
    };

    private void clearTextFields() {
        if (!dialogShown) {
            dialogShown = true;  // Set the flag to true to indicate the dialog has been shown

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Confirm Clear")
                    .setMessage("Are you sure you want to clear the text fields?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (editDialog != null) {
                            // Initialize input fields with the fetched item details from the dialog
                            TextInputEditText itemNameEditText = editDialog.findViewById(R.id.editTextItemName);
                            TextInputEditText itemPriceEditText = editDialog.findViewById(R.id.editTextItemPrice);
                            TextInputEditText itemDescriptionEditText = editDialog.findViewById(R.id.editTextItemDescription);

                            if (itemNameEditText != null && itemPriceEditText != null && itemDescriptionEditText != null) {
                                itemNameEditText.setText("");
                                itemPriceEditText.setText("");
                                itemDescriptionEditText.setText("");
                            }
                        }

                        dialogShown = false;  // Reset the flag when dialog is dismissed
                    })
                    .setOnDismissListener(dialogInterface -> dialogShown = false)  // Reset the flag when dialog is dismissed
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialogShown = false;  // Reset the flag if user cancels
                    })
                    .show();
        }
    }







    private String constructShareMessage() {
        return "Hi, I'm going to travel, can you help me get this " +
                itemNameView.getText() +
                ", it costs around NPR " + itemPriceView.getText() + "." +
                "\n\nItem Description: \n" + itemDescriptionView.getText();
    }

    private void fetchItemDetails(String itemDocumentId) {
        // Get a reference to the item document using its documentId
        firestore.collection("items").document(itemDocumentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // DocumentSnapshot contains the item details
                            itemName = document.getString("itemName");
                            itemPrice = document.getString("itemPrice");
                            itemDescription = document.getString("itemDescription");
                            itemImageUrl = document.getString("itemImage");

                            // Load item details into views
                            itemNameView.setText(itemName);
                            itemPriceView.setText(itemPrice);
                            itemDescriptionView.setText(itemDescription);

                            // Load and display the image
                            loadItemImage(itemImageUrl, itemImageView);


                        } else {
                            // Handle the case where the document doesn't exist
                            // You can display an error message or take appropriate action
                        }
                    } else {
                        // Handle failures (e.g., FirestoreException)
                        // You can display an error message or take appropriate action
                    }
                });
    }


    private void loadItemImage(String imageURL, ImageView itemImageView) {
        // Load image using Glide
        Glide.with(this)
                .load(imageURL)
                .placeholder(R.drawable.image_details_placeholder)  // Placeholder while loading
                .error(R.drawable.item_details_error_placeholder)  // Error image if load fails
                .into(itemImageView);
    }

    private void showEditDialog() {
        // Create a dialog with a custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_input, null);

        // Initialize input fields with the fetched item details
        TextInputEditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);
        TextInputEditText itemPriceEditText = dialogView.findViewById(R.id.editTextItemPrice);
        TextInputEditText itemDescriptionEditText = dialogView.findViewById(R.id.editTextItemDescription);
        imageDialog = dialogView.findViewById(R.id.imageViewItem);
        Button selectButton = dialogView.findViewById(R.id.buttonSelectImage);

        // Load the fetched image into the dialog's ImageView
        loadItemImage(itemImageUrl, imageDialog);
        itemNameEditText.setText(itemName);
        itemPriceEditText.setText(itemPrice);
        itemDescriptionEditText.setText(itemDescription);

        // Set up click listener for the "Select Image" button
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to select an image
                openGallery();
            }
        });

        // Build the MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Retrieve edited details from input fields
                    String editedItemName = itemNameEditText.getText().toString();
                    String editedItemPrice = itemPriceEditText.getText().toString();
                    String editedItemDescription = itemDescriptionEditText.getText().toString();

                    // Update the item details in Firestore
                    updateItemDetails(editedItemName, editedItemPrice, editedItemDescription, selectedImageUri);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                });

        editDialog = dialogBuilder.create();
        editDialog.show();

        // Add an OnTouchListener to the parent view of the dialog to hide the keyboard
        dialogView.setOnTouchListener((v, event) -> {
            hideKeyboard(ItemDetailsActivity.this, dialogView);
            itemNameEditText.clearFocus();
            itemPriceEditText.clearFocus();
            itemDescriptionEditText.clearFocus();
            return false;
        });
    }

    // Defining method to unfocus on the bottom sheet
    public static void hideKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Method to open the gallery for image selection
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
    }

    // Handle the result of selecting an image from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_GALLERY) {
            if (data != null) {
                selectedImageUri = data.getData();
                // Load the selected image into the dialog
                loadItemImage(selectedImageUri.toString(), imageDialog);
            }
        }
    }


    private void updateItemDetails(String editedItemName, String editedItemPrice, String editedItemDescription, Uri newImageUri) {
        // Show the progress dialog
        showProgressDialog();
        // Check if a new image is selected
        if (newImageUri != null) {
            // Upload the new image to Firebase Storage
            uploadImageToFirebaseStorage(newImageUri, editedItemName, editedItemPrice, editedItemDescription);
        } else {
            // No new image selected, update only the item details
            updateItemDetailsInFirestore(editedItemName, editedItemPrice, editedItemDescription, null);
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, String itemName, String itemPrice, String itemDescription) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("images/" + itemName + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Update item details with the new image URL
                        updateItemDetailsInFirestore(itemName, itemPrice, itemDescription, downloadUri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle any errors during the image upload
                    Log.e("ItemDetailsActivity", "Image upload failed: " + e.getMessage());
                });
    }

    private void updateItemDetailsInFirestore(String editedItemName, String editedItemPrice, String editedItemDescription, String imageUrl) {
        // Create a map to hold the updated item details
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("itemName", editedItemName);
        updatedData.put("itemPrice", editedItemPrice);
        updatedData.put("itemDescription", editedItemDescription);

        if (imageUrl != null) {
            updatedData.put("itemImage", imageUrl);
        }

        // Update the item details in Firestore
        firestore.collection("items").document(itemDocumentIdP)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    // Item details updated successfully
                    // Refresh the item details view
                    fetchItemDetails(itemDocumentIdP);
                    // Hide the progress dialog
                    hideProgressDialog();
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to update item details
                    Log.e("ItemDetailsActivity", "Failed to update item details", e);
                    // Hide the progress dialog
                    hideProgressDialog();
                    Toast.makeText(this, "Item updated failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteItem(String itemDocumentId) {
        // Show a confirmation dialog before deleting
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the item from Firestore
                    firestore.collection("items").document(itemDocumentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Item deleted successfully
                                Toast.makeText(ItemDetailsActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();

                                // Navigate back to the item list activity
                                Intent intent = new Intent(ItemDetailsActivity.this, ItemListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Failed to delete item
                                Toast.makeText(ItemDetailsActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled the deletion, do nothing
                })
                .show();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating item...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }




}