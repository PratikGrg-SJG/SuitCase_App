package com.pratikgurung.suitcase;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    Button logoutbtn, next;
    TextView textView1, textView2;
    ImageView image;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        logoutbtn = findViewById(R.id.logout);
        textView1 = findViewById(R.id.textViewMA);
        textView2 = findViewById(R.id.textViewDN);
        next = findViewById(R.id.nextPageBtn);
        toolbar = findViewById(R.id.toolbar);
        image = findViewById(R.id.iv_image);

         //initializing firebase auth
        auth = FirebaseAuth.getInstance();
        //initialize firebase user
        user = auth.getCurrentUser();

        //setting custom toolbar
        setSupportActionBar(toolbar);

        //checking if user exist or not
        if(user ==  null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {

            // When firebase user is not equal to null set image on image view
            Glide.with(MainActivity.this).load(user.getPhotoUrl()).into(image);
            // set name on text view
            textView2.setText(user.getDisplayName());
            textView1.setText(user.getEmail());
        }

        //initialize sign in client
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        //singning out logged in user
        logoutbtn.setOnClickListener(view -> {
            // Sign out from google
            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // Check condition
                    if (task.isSuccessful()) {
                        // When task is successful sign out from firebase
                        showLogoutConfirmationDialog();

                    }
                }
            });
        });
        //moving to next activity making main activity parent
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                startActivity(intent);

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
        } else if (id == R.id.navigation_dashboard) {
            // Handle Exit option click
            // Implement your exit logic here
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
                confirmButton.setTextColor(ContextCompat.getColor(this, R.color.secondaryColor));

            }
}


