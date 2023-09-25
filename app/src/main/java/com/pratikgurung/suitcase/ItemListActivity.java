package com.pratikgurung.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
   /* FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference itemReference = db.collection("items");*/



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
        itemList = new ArrayList<>();
        // Initialize the progressDialog
        progressDialog = new AlertDialog.Builder(this)
                .setView(R.layout.progress_dialog)
                .setCancelable(false)
                .create();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(destinationName); //setting appbar title text
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //enable back arrow button

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
    }


    // Method to add an item to the itemList and update the RecyclerView
    private void addItemToItemList(ItemModel item) {
        itemList.add(item);
        itemAdapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
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
                                        addItemToItemList(item);
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
        itemCollection.whereEqualTo("destinationDocumentId", destinationDocumentId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Convert the Firestore document to an ItemModel object
                            ItemModel item = document.toObject(ItemModel.class);
                            // Add the item to the item list
                            addItemToItemList(item);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure in fetching item data
                        Toast.makeText(ItemListActivity.this, "Failed to fetch items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        intent.putExtra("itemName", item.getItemName());
        intent.putExtra("itemPrice", item.getItemPrice());
        intent.putExtra("itemDescription", item.getItemDescription());
        intent.putExtra("itemImageURL", item.getItemImage());

        // Start the ItemDetailActivity
        startActivity(intent);
    }



}

