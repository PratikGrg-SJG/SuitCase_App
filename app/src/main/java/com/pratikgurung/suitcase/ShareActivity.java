package com.pratikgurung.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    private static final int CONTACT_PICKER_REQUEST = 1;

    MaterialToolbar toolbar;
    ImageView contactLogo;
    TextView contactDetails;
    ImageView imageView;
    TextView textView;
    MaterialButton sendButton;
    LinearLayout shareViaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        toolbar = findViewById(R.id.toolbarShare);
        contactLogo = findViewById(R.id.contactLogo);
        contactDetails = findViewById(R.id.contactDetail);
        imageView = findViewById(R.id.imageItemShare);
        textView = findViewById(R.id.textItemShare);
        sendButton = findViewById(R.id.buttonSend);
        shareViaButton = findViewById(R.id.shareVia);

        // Set the custom toolbar as the support action bar
        setSupportActionBar(toolbar);
        // Enable the Up button (if needed)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactLogo.setOnClickListener(v -> {
            requestContactsPermission();

        });


        // Log the image URL to check if it's received correctly
        String itemImageURL = getIntent().getStringExtra("itemImageURL");
        Log.d("ShareActivity", "Received Image URL: " + itemImageURL);

        // Load the image into the ImageView
        loadItemImage(itemImageURL, imageView);

        // Set the share message in the TextView
        textView.setText(getIntent().getStringExtra("shareMessage"));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if a contact is selected
                String contactDetailsText = contactDetails.getText().toString().trim();
                String shareMessage = textView.getText().toString();
                Log.d("ShareActivity", "Sending message: " + shareMessage);

                if (contactDetailsText.isEmpty()) {
                    Toast.makeText(ShareActivity.this, "Please select a contact first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Extract the phone number from the contact details
                String phoneNumber = extractPhoneNumber(contactDetailsText);

                if (phoneNumber != null) {
                    // Send the SMS
                    sendSMS(phoneNumber, shareMessage);
                } else {
                    Toast.makeText(ShareActivity.this, "Please select a contact", Toast.LENGTH_SHORT).show();
                }
            }
        });

        shareViaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareItem();
            }
        });

    }

    private void loadItemImage(String imageURL, ImageView imageView) {
        Glide.with(this)
                .load(imageURL)
                .placeholder(R.drawable.placeholder_image)  // Placeholder while loading
                .error(R.drawable.item_details_error_placeholder)  // Error image if load fails
                .into(imageView);
    }

    private void requestContactsPermission() {
        // Check if READ_CONTACTS permission is granted
        int readContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int sendSmsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        List<String> permissionsToRequest = new ArrayList<>();

        // Check and add READ_CONTACTS permission if needed
        if (readContactsPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
        }

        // Check and add SEND_SMS permission if needed
        if (sendSmsPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        }

        // Request permissions
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]), CONTACT_PICKER_REQUEST);
        } else {
            // Both permissions are already granted, proceed to load contacts
            loadContacts();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CONTACT_PICKER_REQUEST) {
            boolean allPermissionsGranted = true;

            // Check if all requested permissions are granted
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions granted, proceed to load contacts
                loadContacts();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ShareActivity", "onActivityResult called with requestCode: " + requestCode);

        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == RESULT_OK) {
            Log.d("ShareActivity", "requestCode: " + requestCode + ", resultCode: " + resultCode);

            Uri contactUri = data.getData();

            // Get the contact ID from the URI
            String[] projection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                cursor.close();

                // Use the contact name to get the phone number
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactID},
                        null);

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    int phoneNumberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String phoneNumber = phoneCursor.getString(phoneNumberIndex);

                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        // Set the new contact name and phone number
                        contactDetails.setText("Contact Name: " + contactName + "\nPhone Number: " + phoneNumber);
                    } else {
                        Toast.makeText(this, "No phone number found for this contact error1", Toast.LENGTH_SHORT).show();
                    }

                    phoneCursor.close();
                } else {
                    Toast.makeText(this, "No phone number found for this contact error2", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void loadContacts() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST);
    }

    // Helper method to extract phone number from contact details
    private String extractPhoneNumber(String contactDetailsText) {
        String[] lines = contactDetailsText.split("\n");
        for (String line : lines) {
            if (line.startsWith("Phone Number:")) {
                // Remove "Phone Number: " from the line to get the phone number
                String phoneNumber = line.substring("Phone Number: ".length()).trim();
                Log.d("ShareActivity", "Extracted Phone Number: " + phoneNumber);
                return phoneNumber;
            }
        }
        return null;
    }


    // Helper method to send SMS
    private void sendSMS(String phoneNumber, String message) {
        try {
            // Get the default SMS manager
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();

            // Send the SMS
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            Toast.makeText(ShareActivity.this, "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ShareActivity.this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            Log.e("ShareActivity", "Error sending SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void shareItem() {
        // Construct the share message with the desired format
        String shareMessage = textView.getText().toString();

        // Save the image to the app's files directory
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Uri imageUri = saveImageToFilesDirectory(bitmap);

        // Create a new Intent using ACTION_SEND_MULTIPLE
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("*/*");

        // Set the text and image for sharing
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Create an ArrayList to hold the image URIs
        ArrayList<Uri> imageUris = new ArrayList<>();
        imageUris.add(imageUri);

        // Add both the message and the image to the intent
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);

        // Grant read permissions to the receiving app
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the chooser to share the message and the image
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private Uri saveImageToFilesDirectory(Bitmap bitmap) {
        // Get the application's files directory
        File filesDir = getFilesDir();

        // Create a subdirectory named "images" if not exists
        File imageDir = new File(filesDir, "images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        // Create a file to save the image
        File imageFile = new File(imageDir, "shared_image.jpg");

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the content URI for the saved image
        String packageName = getPackageName();
        return FileProvider.getUriForFile(this, packageName + ".provider", imageFile);
    }

}

