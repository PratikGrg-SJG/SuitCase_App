package com.pratikgurung.suitcase;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.pratikgurung.suitcase.adaptor.DestinationAdaptor;
import com.pratikgurung.suitcase.models.DestinationModel;

public class MainActivity extends AppCompatActivity implements DestinationAdaptor.OnDestinationClickListener {

    FirebaseAuth auth;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;
    MaterialToolbar toolbar;
    FloatingActionButton fabAddDestination;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView recyclerView;
    ImageView imageView;
    TextView textViewEL;
    TextView textViewYD;
    DestinationAdaptor adapter;
    List<DestinationModel> destinations; // Updated to store destination data
    //for shake gesture
    private static final float SHAKE_THRESHOLD = 15f;
    private static final int SHAKE_WAIT_TIME_MS = 500;
    private long lastShakeTime = 0;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        fabAddDestination = findViewById(R.id.fab_add_destination);
        imageView = findViewById(R.id.imageView);
        textViewEL = findViewById(R.id.textViewEL);
        textViewYD = findViewById(R.id.textViewYD);

        // Initialize firebase auth
        auth = FirebaseAuth.getInstance();
        // Initialize firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to collection
        CollectionReference destinationCollection = db.collection("Travel Destination");

        // Initialize firebase user
        user = auth.getCurrentUser();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        // Setting custom toolbar
        setSupportActionBar(toolbar);




        // Checking if user exists or not
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }


        // Adding new destination using floating action button
        fabAddDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_sheet_dialog, (LinearLayout) findViewById(R.id.bottomSheet));
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                // Declare TextInputEditText fields
                TextInputEditText inputDesName = view.findViewById(R.id.destinationName);
                TextInputEditText inputNote = view.findViewById(R.id.note);
                ImageView datePickerButton = view.findViewById(R.id.datePicker);
                TextView datePickerView = view.findViewById(R.id.dptextView);

                datePickerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select Date").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
                        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                            @Override
                            public void onPositiveButtonClick(Long selection) {
                                String date = new SimpleDateFormat("E, dd MMM, yyyy", Locale.getDefault()).format(new Date(selection));
                                datePickerView.setText(MessageFormat.format("{0}", date));
                            }
                        });
                        materialDatePicker.show(getSupportFragmentManager(), "tag");
                    }
                });

                detectShake(); //detect shake

                // Add an OnTouchListener to the parent view of the dialog
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Hide the keyboard
                        hideKeyboard(MainActivity.this, view);
                        // Clear focus from TextInputEditText fields
                        inputDesName.clearFocus();
                        inputNote.clearFocus();
                        return false;
                    }
                });

                // Declare button to save data
                Button saveButton = view.findViewById(R.id.save_destination_btn);

                // Inside the saveButton click listener
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                                                // Get user input
                        String destinationName = inputDesName.getText().toString();
                        String note = inputNote.getText().toString();
                        String selectedDate = datePickerView.getText().toString();

                        bottomSheetDialog.dismiss(); // Close the bottom sheet

                        // Create a new destination document
                        Map<String, Object> destinationData = new HashMap<>();
                        destinationData.put("destinationName", destinationName);
                        destinationData.put("notes", note);
                        destinationData.put("selectedDate", selectedDate);
                        // Associate the destination with the user using userId
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        destinationData.put("userId", userId);

                        // Add the document to the collection
                        db.collection("Travel Destination")
                                .add(destinationData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Document added successfully
                                        Toast.makeText(MainActivity.this, "Destination added!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                        Toast.makeText(MainActivity.this, "Failed to add destination: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

            }


        });

        // For fetching data from Firebase
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        destinations = new ArrayList<>(); // Initialize the list
        adapter = new DestinationAdaptor(destinations, this, this, this); // Pass destinations and this as listener
        recyclerView.setAdapter(adapter);

        // Set up a Firestore real-time listener to update data in real-time
        destinationCollection.whereEqualTo("userId", user.getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                // Checking in real-time data in collection
                boolean isEmpty = queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty();

                // Update views based on the empty status calling the method
                updateViewsBasedOnEmptyStatus(isEmpty);

                destinations.clear(); // Clear the list before adding data
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    DestinationModel destination = document.toObject(DestinationModel.class);
                    destination.setDocumentId(document.getId()); //Set the documentId
                    destinations.add(destination);
                    Log.d("Firestore", "Fetched: " + destination.getDestinationName());
                }
                adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
            }
        });

        // Set up swipe gestures to update and delete functionality
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                private int iconSize;  // Adjusted icon size

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int adapterPosition = viewHolder.getAdapterPosition();

                    if (direction == ItemTouchHelper.LEFT) {
                        // Delete on swipe left
                        onDeleteClick(adapterPosition);
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        // Update on swipe right
                        onUpdateClick(adapterPosition);
                    }
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        View itemView = viewHolder.itemView;

                        // Calculate the icon size based on the itemView dimensions
                        iconSize = (int) (0.7 * itemView.getHeight());

                        Paint paint = new Paint();
                        if (dX > 0) {
                            // Swiping right (edit)
                            paint.setColor(ContextCompat.getColor(MainActivity.this, R.color.teal));
                            Drawable editIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_edit); // Add your edit icon
                            // Calculate icon position to keep it centered horizontally
                            float iconMargin = (itemView.getHeight() - iconSize) / 2.0f;
                            float iconLeft = itemView.getLeft() + iconMargin;
                            float iconRight = iconLeft + iconSize;

                            // Draw edit background with rounded corners
                            RectF background = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom());
                            float cornerRadius = 20.0f;  // Adjust the corner radius
                            c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                            // Draw edit icon
                            editIcon.setBounds(
                                    (int) iconLeft,
                                    (int) (itemView.getTop() + iconMargin),
                                    (int) iconRight,
                                    (int) (itemView.getTop() + iconMargin + iconSize)
                            );
                            editIcon.draw(c);
                        } else {
                            // Swiping left (delete)
                            paint.setColor(ContextCompat.getColor(MainActivity.this, R.color.md_theme_dark_errorContainer));
                            Drawable deleteIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete); // Add your delete icon
                            float iconMargin = (itemView.getHeight() - iconSize) / 2.0f;  // Use the adjusted icon size
                            float iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                            float iconBottom = iconTop + iconSize;


                            // Draw delete background with rounded corners
                            RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                            float cornerRadius = 20.0f;  // Adjust the corner radius
                            c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                            // Draw delete icon
                            deleteIcon.setBounds(
                                    (int) (itemView.getRight() - iconMargin - iconSize),
                                    (int) iconTop,
                                    (int) (itemView.getRight() - iconMargin),
                                    (int) iconBottom
                            );
                            deleteIcon.draw(c);
                        }
                    }
                }

            }).attachToRecyclerView(recyclerView);




        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navigation_home) {
            // Handle Settings option click
            return true;
        } else if (id == R.id.navigation_dashboard) {
            // Handle About option click
            return true;
        } else if (id == R.id.navigation_notifications) {
            // Handle Exit option click
            // Implement your exit logic here
            return true;
        } else if (id == R.id.navigation_logout) {
            showLogoutConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Showing a dialog box during signout to confirm or cancel
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        auth.signOut();
                        // Display Toast
                        Toast.makeText(getApplicationContext(), "Successfully Logged Out", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();

        // Applying a custom rounded background drawable to the dialog's window
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);

        // Set a flag to prevent the dialog from getting cut off
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();

        // Make the dialog visible and centered
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // Get dimension values for the dialog box from resources
        int dialogWidth = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dialog_height);

        // Adjust the dialog box size (width and height)
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = dialogWidth;
        params.height = dialogHeight;
        dialog.getWindow().setAttributes(params);

        // Changing text color of Confirm to be red
        Button confirmButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        confirmButton.setTextColor(ContextCompat.getColor(this, R.color.md_theme_light_error));

        // Changing text color of Confirm to be grey
        Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelButton.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_surfaceVariant));
    }

    // Defining method to unfocus on the bottom sheet
    public static void hideKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Method to update views based on the empty status of collection
    private void updateViewsBasedOnEmptyStatus(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            textViewYD.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            textViewEL.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textViewYD.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            textViewEL.setVisibility(View.GONE);
        }
    }

    // Implement the onDeleteClick method
    @Override
    public void onDeleteClick(int position) {
        showDeleteConfirmationDialog(position);
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this destination?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the item from the database
                        deleteDestinationFromDatabase(position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(position);  // Notify adapter to redraw the item
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();

        // Applying a custom rounded background drawable to the dialog's window
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);

        // Set a flag to prevent the dialog from getting cut off
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();

        // Make the dialog visible and centered
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // Get dimension values for the dialog box from resources
        int dialogWidth = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dialog_height);

        // Adjust the dialog box size (width and height)
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = dialogWidth;
        params.height = dialogHeight;
        dialog.getWindow().setAttributes(params);

        // Changing text color of Confirm to be red
        Button confirmButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        confirmButton.setTextColor(ContextCompat.getColor(this, R.color.md_theme_light_error));

        // Changing text color of Confirm to be grey
        Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelButton.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_surfaceVariant));
    }

    private void deleteDestinationFromDatabase(int position) {
        DestinationModel destination = destinations.get(position);
        String documentId = destination.getDocumentId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Travel Destination")
                .document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Document successfully deleted from the database
                        Toast.makeText(MainActivity.this, "Destination Removed successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(MainActivity.this, "Failed to remove destination" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onUpdateClick(int position) {
        DestinationModel destination = destinations.get(position);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        showUpdateDialog(destination, position, userId);
    }

    private void showUpdateDialog(DestinationModel destination, int position, String userId) {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_sheet_dialog, (LinearLayout) findViewById(R.id.bottomSheet));
        bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        // Modify the dialog to allow updating the data
        TextInputEditText inputDesName = view.findViewById(R.id.destinationName);
        TextInputEditText inputNote = view.findViewById(R.id.note);
        ImageView datePickerButton = view.findViewById(R.id.datePicker);
        TextView datePickerView = view.findViewById(R.id.dptextView);

        // Set the existing data in the dialog
        inputDesName.setText(destination.getDestinationName());
        inputNote.setText(destination.getNotes());
        datePickerView.setText(destination.getSelectedDate());

        // Update button for updating the destination
        Button updateButton = view.findViewById(R.id.save_destination_btn);
        updateButton.setText("Update");

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select Date").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        String date = new SimpleDateFormat("E, dd MMM, yyyy", Locale.getDefault()).format(new Date(selection));
                        datePickerView.setText(MessageFormat.format("{0}", date));
                    }
                });
                materialDatePicker.show(getSupportFragmentManager(), "tag");
            }
        });

        // Add an OnTouchListener to the parent view of the dialog
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide the keyboard
                hideKeyboard(MainActivity.this, view);
                // Clear focus from TextInputEditText fields
                inputDesName.clearFocus();
                inputNote.clearFocus();
                return false;
            }
        });

        // Inside the updateButton click listener
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get updated user input
                String updatedDestinationName = inputDesName.getText().toString();
                String updatedNote = inputNote.getText().toString();
                String updatedSelectedDate = datePickerView.getText().toString();

                bottomSheetDialog.dismiss(); // Close the bottom sheet

                // Update the destination document in the database
                updateDestinationInDatabase(destination, updatedDestinationName, updatedNote, updatedSelectedDate, position, userId);
            }
        });
    }

    private void updateDestinationInDatabase(DestinationModel destination, String updatedDestinationName, String updatedNote, String updatedSelectedDate, int position, String userId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("destinationName", updatedDestinationName);
        updateData.put("notes", updatedNote);
        updateData.put("selectedDate", updatedSelectedDate);
        updateData.put("userId", userId);

        db.collection("Travel Destination")
                .document(destination.getDocumentId())
                .update(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Destination successfully updated in the database
                        Toast.makeText(MainActivity.this, "Destination updated successfully", Toast.LENGTH_SHORT).show();

                        // Update the item in the RecyclerView
                        adapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(MainActivity.this, "Failed to update destination: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




    @Override
    public void onItemClick(DestinationModel destination) {

        String documentId = destination.getDocumentId();
        // Handle the item click to navigate to ItemListActivity
        Intent intent = new Intent(MainActivity.this, ItemListActivity.class);
        intent.putExtra("destinationName", destination.getDestinationName());
        intent.putExtra("documentId", documentId);

        Log.d("Firestore", "Destination Name: " + destination.getDestinationName());
        Log.d("Firestore", "Document ID: " + destination.getDocumentId());


        startActivity(intent);
    }

    //methods for shake gesture
    private boolean isBottomSheetVisible = false;

    private void detectShake() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastShakeTime) > SHAKE_WAIT_TIME_MS) {
            lastShakeTime = currentTime;
            if (isBottomSheetVisible) {
                // Handle shake action here for the bottom sheet
                clearFieldsWithConfirmation();
            }
        }
    }

    private void clearFieldsWithConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear Fields")
                .setMessage("Are you sure you want to clear all fields?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear the fields
                        clearFields();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clearFields() {
        TextInputEditText inputDesName = bottomSheetDialog.findViewById(R.id.destinationName);
        TextInputEditText inputNote = bottomSheetDialog.findViewById(R.id.note);
        TextView datePickerView = bottomSheetDialog.findViewById(R.id.dptextView);

        if (inputDesName != null) {
            inputDesName.setText("");
        }
        if (inputNote != null) {
            inputNote.setText("");
        }
        if (datePickerView != null) {
            datePickerView.setText("Pick Date");
        }
    }


}
