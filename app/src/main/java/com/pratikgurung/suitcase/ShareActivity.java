package com.pratikgurung.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;


public class ShareActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    ImageView imageView;
    TextView textView;
    MaterialButton sendButton;
    private static final int CONTACTS_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        toolbar = findViewById(R.id.toolbarShare);
        imageView = findViewById(R.id.imageItemShare);
        textView = findViewById(R.id.textItemShare);
        sendButton = findViewById(R.id.buttonSend);

        // Set the custom toolbar as the support action bar
        setSupportActionBar(toolbar);
        // Enable the Up button (if needed)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String itemImageURL = intent.getStringExtra("itemImageURL");
        String shareMessage = intent.getStringExtra("shareMessage");

        requestContactsPermission();

        // Log the image URL to check if it's received correctly
        Log.d("ShareActivity", "Received Image URL: " + itemImageURL);

        // Load the image into the ImageView
        loadItemImage(itemImageURL, imageView);

        // Set the share message in the TextView
        textView.setText(shareMessage);
    }

    private void loadItemImage(String imageURL, ImageView imageView) {
        Glide.with(this)
                .load(imageURL)
                .placeholder(R.drawable.placeholder_image)  // Placeholder while loading
                .error(R.drawable.item_details_error_placeholder)  // Error image if load fails
                .into(imageView);
    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACTS_PERMISSION_REQUEST);
        } else {
            // Permission already granted, proceed to load contacts
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to load contacts
                loadContacts();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadContacts() {
        // TODO: Retrieve contacts and display them in your UI
        // You can use ContentResolver to query contacts and populate a list in your UI
        // Example:
        // Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        // ...
        // Display contacts in your UI
    }
}
