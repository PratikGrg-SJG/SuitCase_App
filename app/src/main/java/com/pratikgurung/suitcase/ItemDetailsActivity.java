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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
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
    MaterialButton shareButton;
    LinearLayout editItemID, deleteItemID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

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

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the ShareActivity
                Intent intent = new Intent(ItemDetailsActivity.this, ShareActivity.class);

                // Pass the item image URL and share message as extras to the intent
                intent.putExtra("itemImageURL", itemImageURL);  // Use the variable directly
                intent.putExtra("shareMessage", constructShareMessage());

                startActivity(intent);
            }
        });

    }
    private String constructShareMessage() {
        return "Hi, I'm going to travel, can you help me get this " +
                itemNameView.getText() +
                ", it costs around NPR " + itemPriceView.getText() + "." +
                "\n\nItem Description: \n" + itemDescriptionView.getText();
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
}