package com.pratikgurung.suitcase;


import static android.content.ContentValues.TAG;
import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import models.DestinationModel;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;
    MaterialToolbar toolbar;
    FloatingActionButton fabAddDestination;
    BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.toolbar);
        fabAddDestination = findViewById(R.id.fab_add_destination);
        ImageView imageView = findViewById(R.id.imageView);
        TextView textView = findViewById(R.id.textView);

         //initializing firebase auth
        auth = FirebaseAuth.getInstance();
        //initializing firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //referencing to collection
        CollectionReference destinationCollection = db.collection("Travel Destination");


        //initialize firebase user
        user = auth.getCurrentUser();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        //setting custom toolbar
        setSupportActionBar(toolbar);

        //checking if user exist or not
        if(user ==  null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        //checking if the collection is empty or not
        destinationCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                }
            }
        });




        //adding new destination using floating action button
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

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get user input
                        String destinationName = inputDesName.getText().toString();
                        String note = inputNote.getText().toString();
                        String selectedDate = datePickerView.getText().toString();

                        // Create a new destination document
                        Map<String, Object> destinationData = new HashMap<>();
                        destinationData.put("destinationName", destinationName);
                        destinationData.put("notes", note);
                        destinationData.put("selectedDate", selectedDate);

                        db.collection("Travel Destination")
                                .add(destinationData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Document added successfully
                                        Toast.makeText(MainActivity.this, "Destination added!", Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss(); // Close the bottom sheet
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

        //for fectching data from firebase
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DestinationAdaptor adapter = new DestinationAdaptor(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Set up a Firestore real-time listener to update data in real-time
        destinationCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                List<DestinationModel> destinations = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    DestinationModel destination = document.toObject(DestinationModel.class);
                    destinations.add(destination);
                    Log.d("Firestore", "Fetched: " + destination.getDestinationName());
                }
                adapter.setData(destinations); // Update the RecyclerView
            }
        });


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


            //showing dialog box during signout to confirm or cancel
            private void showLogoutConfirmationDialog () {
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

                // Applying custom rounded background drawable to the dialog's window
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);

                // Set a flag to prevent the dialog from getting cut off
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                dialog.show();

                // Make the dialog visible and centered
                dialog.getWindow().getDecorView().setSystemUiVisibility(
                        this.getWindow().getDecorView().getSystemUiVisibility());
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                // Get dimension values for dialog box from resources
                int dialogWidth = getResources().getDimensionPixelSize(R.dimen.dialog_width);
                int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dialog_height);

                // Adjust the dialog box size (width and height)
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = dialogWidth;
                params.height = dialogHeight;
                dialog.getWindow().setAttributes(params);

                //changing text color of confirm to be red
                Button confirmButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                confirmButton.setTextColor(ContextCompat.getColor(this, R.color.md_theme_light_error));

            }

            //defining method to unfocus on the bottom sheet
            public static void hideKeyboard(Activity activity, View view) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
    }

}


