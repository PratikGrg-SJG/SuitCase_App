package com.pratikgurung.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.pratikgurung.suitcase.adaptor.ItemAdaptor;
import com.pratikgurung.suitcase.models.ItemModel;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity implements ItemAdaptor.OnItemClickListener {

    MaterialToolbar toolbar;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference storageRef;
    private FirebaseFirestore firestore;
    private CollectionReference itemCollection;
    private Uri imageUri;
    private ItemAdaptor itemAdapter;
    private List<ItemModel> itemList;
    private AlertDialog progressDialog;
    private ImageView image;
    private LinearLayout noItemsLayout;
    private TextView noItemsTextView;
    private TabLayout tabLayout;
    private int selectedTabPosition = 0; // Default to "To Purchase" tab
    private ListenerRegistration itemSnapshotListener;
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
        setContentView(R.layout.activity_item_list);

        // Retrieve destination name and documentId
        Intent intent = getIntent();
        String destinationName = intent.getStringExtra("destinationName");
        String destinationDocumentId = intent.getStringExtra("documentId");

        // Log the values
        Log.d("ItemListActivity", "Destination Name: " + destinationName);
        Log.d("ItemListActivity", "Document ID: " + destinationDocumentId);

        toolbar = findViewById(R.id.toolbarItems);
        recyclerView = findViewById(R.id.recyclerViewItems);
        floatingActionButton = findViewById(R.id.fabAddItems);
        tabLayout = findViewById(R.id.tabLayout);
        noItemsTextView = findViewById(R.id.noItemsTextView);

        itemList = new ArrayList<>();
        // Initialize the progressDialog
        progressDialog = new AlertDialog.Builder(this)
                .setView(R.layout.progress_dialog)
                .setCancelable(false)
                .create();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(destinationName); //setting appbar title text
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //enable back arrow button

        // Listen for tab selection changes
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabPosition = tab.getPosition();
                fetchItemData(destinationDocumentId);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed for this implementation
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed for this implementation
                selectedTabPosition = tab.getPosition();
                fetchItemData(destinationDocumentId);
            }
        });

        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        itemCollection = firestore.collection("items");

        fetchItemData(destinationDocumentId);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showItemInputDialog(destinationDocumentId);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdaptor(itemList, this, (ItemAdaptor.OnItemClickListener) this);
        recyclerView.setAdapter(itemAdapter);

        // Create a SimpleCallback for swipe gestures (left and right)
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ItemModel item = itemList.get(position);

                // Handle swipe left (delete)
                if (direction == ItemTouchHelper.LEFT) {
                    deleteItem(item);
                }

                // Handle swipe right (edit)
                if (direction == ItemTouchHelper.RIGHT) {
                    editItem(item);
                }
            }
        };

// Create an ItemTouchHelper and attach it to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //for shake gesture
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration = 10f;  // You can adjust the sensitivity as needed
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

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
    private void deleteItem(ItemModel item) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete this item?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            // User clicked Delete button
            deleteItemFromFirestore(item);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // User cancelled the deletion
            itemAdapter.notifyDataSetChanged();
            dialog.dismiss();
        }).setOnCancelListener(dialog -> {
            // Dialog canceled, notify the adapter to refresh the view
            itemAdapter.notifyDataSetChanged();
        });;
        builder.show();
    }


    private void deleteItemFromFirestore(ItemModel item) {
        String itemDocumentId = item.getItemDocumentId();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference itemsCollection = firestore.collection("items");

        itemsCollection.document(itemDocumentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Item deleted successfully from Firestore
                    // Now remove it from the local list and notify the adapter
                    itemList.remove(item);
                    itemAdapter.notifyDataSetChanged();

                    // Display appropriate message based on the list
                    updateNoItemsMessage();

                    Toast.makeText(ItemListActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to delete item from Firestore
                    Toast.makeText(ItemListActivity.this, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void editItem(ItemModel item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.item_input, null);

        // Initialize the dialog components
        image = dialogView.findViewById(R.id.imageViewItem);
        Button selectImage = dialogView.findViewById(R.id.buttonSelectImage);
        TextInputLayout itemNameLayout = dialogView.findViewById(R.id.textInputItemName);
        TextInputEditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);
        TextInputLayout itemPriceLayout = dialogView.findViewById(R.id.textInputItemPrice);
        TextInputEditText itemPriceEditText = dialogView.findViewById(R.id.editTextItemPrice);
        TextInputLayout itemDescriptionLayout = dialogView.findViewById(R.id.textInputItemDescription);
        TextInputEditText itemDescriptionEditText = dialogView.findViewById(R.id.editTextItemDescription);

        // Pre-fill the item details
        itemNameEditText.setText(item.getItemName());
        itemPriceEditText.setText(item.getItemPrice());
        itemDescriptionEditText.setText(item.getItemDescription());

        // Load the image if available
        if (item.getItemImage() != null) {
            // Load the image using an image loading library like Glide or Picasso
            // For simplicity, we assume a function loadImageIntoImageView(image, item.getItemImage()) to load the image
            loadImageIntoImageView(image, item.getItemImage());
        }

        selectImage.setOnClickListener(v -> {
            // Open the gallery to pick an image
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Extract edited details from the dialog
                    String editedItemName = itemNameEditText.getText().toString();
                    String editedItemPrice = itemPriceEditText.getText().toString();
                    String editedItemDescription = itemDescriptionEditText.getText().toString();

                    // Update the item details
                    item.setItemName(editedItemName);
                    item.setItemPrice(editedItemPrice);
                    item.setItemDescription(editedItemDescription);

                    // Check if a new image is selected for editing
                    if (imageUri != null) {
                        progressDialog.show();
                        // Upload the edited image to Firebase Storage and update item details
                        uploadImageAndUpdateItem(item, imageUri);
                    } else {
                        // Update item details without changing the image
                        updateItemInFirestore(item);
                    }
                    itemAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    itemAdapter.notifyDataSetChanged();
                    dialog.dismiss();}).setOnCancelListener(dialog -> itemAdapter.notifyDataSetChanged());

        editDialog = builder.create();
        editDialog.show();

        // Add an OnTouchListener to the parent view of the dialog to hide the keyboard
        dialogView.setOnTouchListener((v, event) -> {
            hideKeyboard(ItemListActivity.this, dialogView);
            itemNameLayout.clearFocus();
            itemPriceEditText.clearFocus();
            itemDescriptionEditText.clearFocus();
            return false;
        });
    }

    // Function to update item details and image in Firestore
    private void uploadImageAndUpdateItem(ItemModel item, Uri imageUri) {
        StorageReference imageRef = storageRef.child("images/" + item.getItemName() + ".jpg");

        // Upload the image to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Update item details with the new image URL
                        item.setItemImage(downloadUri.toString());
                        // Update item details in Firestore
                        updateItemInFirestore(item);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle any errors during the image upload
                    Toast.makeText(ItemListActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    // Function to load an image into an ImageView using Glide or Picasso
    private void loadImageIntoImageView(ImageView imageView, String imageUrl) {
        Glide.with(this).load(imageUrl).into(imageView);
    }

    // Function to update item details in Firestore
    private void updateItemInFirestore(ItemModel item) {
        // Update the item details in Firestore
        firestore.collection("items")
                .document(item.getItemDocumentId())
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    // Item details updated successfully
                    Toast.makeText(ItemListActivity.this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    // Failed to update item details
                    Toast.makeText(ItemListActivity.this, "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    private void showItemInputDialog(String destinationDocumentId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.item_input, null);

        image = dialogView.findViewById(R.id.imageViewItem);

        Button selectImage = dialogView.findViewById(R.id.buttonSelectImage);

        TextInputLayout itemNameLayout = dialogView.findViewById(R.id.textInputItemName);
        TextInputEditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);

        TextInputLayout itemPriceLayout = dialogView.findViewById(R.id.textInputItemPrice);
        TextInputEditText itemPriceEditText = dialogView.findViewById(R.id.editTextItemPrice);

        TextInputLayout itemDescriptionLayout = dialogView.findViewById(R.id.textInputItemDescription);
        TextInputEditText itemDescriptionEditText = dialogView.findViewById(R.id.editTextItemDescription);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to pick an image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("New Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String itemName = itemNameEditText.getText().toString();
                    String itemPrice = itemPriceEditText.getText().toString();
                    String itemDescription = itemDescriptionEditText.getText().toString();

                    // Check if an image is selected
                    if (imageUri != null) {
                        progressDialog.show();
                        // Upload image to Firebase Storage and get the download URL
                        uploadImageToFirebaseStorage(imageUri, itemName, itemPrice, itemDescription, destinationDocumentId);
                    } else {
                        Toast.makeText(ItemListActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    }


                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Add an OnTouchListener to the parent view of the dialog
        dialogView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide the keyboard
                hideKeyboard(ItemListActivity.this, dialogView);
                // Clear focus from TextInputEditText fields
                itemNameLayout.clearFocus();
                itemPriceEditText.clearFocus();
                itemDescriptionEditText.clearFocus();
                return false;
            }
        });

    }

    // Upload image to Firebase Storage and save item details to Firebase Firestore
    private void uploadImageToFirebaseStorage(Uri imageUri, String itemName, String itemPrice, String itemDescription, String destinationDocumentId) {
        // Create a storage reference for the image
        StorageReference imageRef = storageRef.child("images/" + itemName + ".jpg");
        // Upload the image to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully, get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {

                                // Save item details to Firebase Firestore
                                saveItemToFirestore(itemName, itemPrice, itemDescription, downloadUri.toString(), destinationDocumentId);
                                progressDialog.dismiss();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors during the image upload
                        Toast.makeText(ItemListActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    // Save item details to Firebase Firestore
    private void saveItemToFirestore(String itemName, String itemPrice, String itemDescription, String imageURL, String destinationDocumentId) {

        // Create a new ItemModel object with the item details including destinationDocumentId
        ItemModel item = new ItemModel(itemName, itemDescription, itemPrice, imageURL, destinationDocumentId);

        // Add the item to Firebase Firestore
        firestore.collection("items")
                .add(item)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Set the item document ID for the item
                        String itemDocumentId = documentReference.getId();
                        item.setItemDocumentId(itemDocumentId); // Set the itemDocumentId

                        // Update the item in Firestore with the correct itemDocumentId
                        documentReference.update("itemDocumentId", itemDocumentId)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Add the item to the item list and update the RecyclerView
                                        itemAdapter.notifyDataSetChanged();
                                        // Item added to Firestore successfully
                                        Toast.makeText(ItemListActivity.this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle any errors during item addition to Firestore
                                        Toast.makeText(ItemListActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors during item addition to Firestore
                        Toast.makeText(ItemListActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void fetchItemData(String destinationDocumentId) {
        boolean isToPurchase = selectedTabPosition == 0;

        // Stop the previous listener if it exists
        if (itemSnapshotListener != null) {
            itemSnapshotListener.remove();
        }

        itemCollection.whereEqualTo("destinationDocumentId", destinationDocumentId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("ItemListActivity", "Listen failed.", e);
                            return;
                        }

                        itemList.clear(); // Clear previous items
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Convert the Firestore document to an ItemModel object
                            ItemModel item = document.toObject(ItemModel.class);

                            // Check if the item belongs to the selected tab
                            if (isToPurchase && !item.isPurchased() && !itemList.contains(item)) {
                                // For "To Purchase" tab, show only unpurchased items
                                itemList.add(item);

                            } else if (!isToPurchase && item.isPurchased()) {
                                // For "Purchased" tab, show only purchased items
                                itemList.add(item);

                            }
                        }

                        // Display appropriate message based on the list
                        updateNoItemsMessage();

                        // Notify the adapter that the data has changed
                        itemAdapter.notifyDataSetChanged();
                    }
                });
    }




    private void updateNoItemsMessage() {
        boolean isToPurchase = selectedTabPosition == 0;

        if (itemList.isEmpty()) {
            if (isToPurchase) {
                noItemsTextView.setText("No items to show, add new");
                floatingActionButton.setVisibility(View.VISIBLE);  // Make sure FAB is visible for "To Purchase" tab
            } else {
                noItemsTextView.setText("No items purchased");
                floatingActionButton.setVisibility(View.GONE);
            }
            // Display the message
            noItemsTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // Hide the message and show the RecyclerView if there are items
            noItemsTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (isToPurchase) {
                floatingActionButton.setVisibility(View.VISIBLE);  // Set FAB to visible for "To Purchase" tab
            } else {
                floatingActionButton.setVisibility(View.GONE);  // Hide FAB for "Purchased" tab
            }
        }
    }

    // Handle the result of image picking
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // Save the image URI for later use to upload in firebase
            this.imageUri = data.getData();
            // Set the selected image to the ImageView
            if (imageUri != null) {
                image.setImageURI(imageUri);
            }
        }
    }

    // Defining method to unfocus on the bottom sheet
    public static void hideKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onItemClick(ItemModel item) {
        // Create an intent to start the ItemDetailActivity
        Intent intent = new Intent(ItemListActivity.this, ItemDetailsActivity.class);

        // Pass the item details to the ItemDetailActivity
        intent.putExtra("itemDocumentId", item.getItemDocumentId());

        // Start the ItemDetailActivity
        startActivity(intent);
    }

}

