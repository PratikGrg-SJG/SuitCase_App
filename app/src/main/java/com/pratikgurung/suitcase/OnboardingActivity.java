package com.pratikgurung.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.pratikgurung.suitcase.adaptor.OnboardingPagerAdaptor;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MaterialButton nextButton;
    private OnboardingPagerAdaptor adaptor;
    private int[] images = {R.drawable.onboard1, R.drawable.onboard2, R.drawable.onboard3};
    private String[] texts = {"Choose \nYour Travel \nDestination!", "Explore \nthe Beautiful \nWorld!", "Pack \nYour Stuff \nin Easiest Way!"};
    private  int currentPage = 0;
    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        adaptor = new OnboardingPagerAdaptor(this, images, texts);
        viewPager.setAdapter(adaptor);
        nextButton = findViewById(R.id.nextButtonOB);

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("OnboardingActivity", "Current Page: " + currentPage);
                Log.d("OnboardingActivity", "adapter count: " + adaptor.getCount());
                if(currentPage < adaptor.getCount() -1){
                    currentPage++;
                    viewPager.setCurrentItem(currentPage);
                }else if(currentPage == adaptor.getCount() - 1){
                    nextButton.setText("Let's Get Started");
                    navigateToLogin();
                }
            }
        });

        setupIndicator();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("onboarding", Context.MODE_PRIVATE);

    }

    private void setupIndicator() {
        LinearLayout indicatorLayout = findViewById(R.id.indicatorLayoutOB);
        final ImageView[] indicators = new ImageView[images.length];

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageResource(R.drawable.indicator_inactive);
            indicators[i].setVisibility(View.VISIBLE);


            // Set layout parameters for the ImageView
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    30, 30);
            layoutParams.setMargins(4, 0, 4, 0);
            indicators[i].setLayoutParams(layoutParams);


            // Add the ImageView to the indicatorLayout
            indicatorLayout.addView(indicators[i]);

            Log.d("OnboardingActivity", "Indicator added at position: " + i);
        }

        // Set the initial active indicator
        indicators[0].setImageResource(R.drawable.indicator_active);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Log.d("OnboardingActivity", "Page selected: " + position);

                currentPage = position;

                for (int i = 0; i < indicators.length; i++) {
                    indicators[i].setImageResource(i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive);
                    Log.d("OnboardingActivity", "Indicator " + i + " set to " + (i == position ? "active" : "inactive"));
                }
                // If it's the last page, update the button text
                if (position == adaptor.getCount() - 1) {
                    nextButton.setText("Let's Get Started");
                } else {
                    nextButton.setText("Next");
                }

            }


            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
    public void navigateToLogin(){
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        markOnboardingAsCompleted();
    }

    private void markOnboardingAsCompleted() {
        // Mark onboarding as completed using SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isCompleted", true);
        editor.apply();
    }
}