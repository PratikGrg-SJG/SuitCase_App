package com.pratikgurung.suitcase;

import static com.google.firebase.database.collection.BuildConfig.APPLICATION_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.collection.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ItemDetailsActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    ImageView itemImageView;
    TextView itemNameView;
    TextView itemPriceView;
    TextView itemDescriptionView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        toolbar = findViewById(R.id.toolbarItemsDetail);
        itemImageView = findViewById(R.id.imageViewItemDetail);
        itemNameView = findViewById(R.id.textViewItemDetailName);
        itemPriceView = findViewById(R.id.textViewItemDetailPrice);
        itemDescriptionView = findViewById(R.id.textViewItemDetailDescription);


        // Retrieve data from intent
        Intent intent = getIntent();
        String itemName = intent.getStringExtra("itemName");
        String itemPrice = intent.getStringExtra("itemPrice");
        String itemDescription = intent.getStringExtra("itemDescription");
        String itemImageURL = intent.getStringExtra("itemImageURL");

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Item Details"); //setting appbar title text
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //enable back arrow button

        // Load item details into views
        itemNameView.setText(itemName);
        itemPriceView.setText(itemPrice);
        itemDescriptionView.setText(itemDescription);
        loadItemImage(itemImageURL, itemImageView);


    }

    //inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            // Call the shareItem method when the share button is clicked
            shareItem();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadItemImage(String imageURL, ImageView imageView) {
        // Load image using your preferred image loading library
        // For example, using Glide:
        Glide.with(this)
                .load(imageURL)
                .placeholder(R.drawable.image_details_placeholder)  // Placeholder while loading
                .error(R.drawable.item_details_error_placeholder)  // Error image if load fails
                .into(imageView);
    }

    private void shareItem() {
        // Construct the share message with the desired format
        String shareMessage = "Hi, I'm going to travel, can you help me get this " +
                itemNameView.getText() +
                ", it costs around NPR " + itemPriceView.getText() + "." +
                "\n\nItem Description: \n" + itemDescriptionView.getText();

        // Save the image to the app's files directory
        BitmapDrawable drawable = (BitmapDrawable) itemImageView.getDrawable();
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