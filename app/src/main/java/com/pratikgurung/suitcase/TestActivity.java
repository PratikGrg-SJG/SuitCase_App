package com.pratikgurung.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;

public class TestActivity extends AppCompatActivity {

    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        toolbar = findViewById(R.id.toolbar);

        //setting custom toolbar
        setSupportActionBar(toolbar);

        // Enable Up navigation in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Change the color of the navigation icon
        Drawable navIcon = toolbar.getNavigationIcon();
        navIcon.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);

        // Increase the size of the navigation icon
        int iconSize = getResources().getDimensionPixelSize(R.dimen.custom_icon_size);
        navIcon.setBounds(0, 0, iconSize, iconSize);

    }
}