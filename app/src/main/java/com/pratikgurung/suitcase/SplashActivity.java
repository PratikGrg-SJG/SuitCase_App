package com.pratikgurung.suitcase;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import java.lang.reflect.Method;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    TextView splashText;
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("onboarding", MODE_PRIVATE);

        splashText = findViewById(R.id.splashText);

            // Create a sequence of animations
            AnimationSet animationSet = new AnimationSet(true);

            // Bounce animation
            Animation bounceAnimation = new ScaleAnimation(
                    0.5f, 1.0f, 0.5f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            bounceAnimation.setDuration(1000);
            bounceAnimation.setInterpolator(new BounceInterpolator());
            animationSet.addAnimation(bounceAnimation);

            // Fade-in animation
            Animation fadeInAnimation = new AlphaAnimation(0, 1);
            fadeInAnimation.setDuration(500);
            fadeInAnimation.setStartOffset(500); // Start after the bounce animation
            animationSet.addAnimation(fadeInAnimation);

            // Apply the animation set to the text
            splashText.startAnimation(animationSet);

            // Change color and size of text
            String text = "Pack Your Picks";
            Spannable spannable = new SpannableString(text);
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Color for "Pack"
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#00576F")), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Color for "Your"
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), 10, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Color for "Picks"
            spannable.setSpan(new RelativeSizeSpan(1.2f), 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Slightly larger size for "Your"

            splashText.setText(spannable);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Fade-out animation
                Animation fadeOutAnimation = new AlphaAnimation(1, 0);
                fadeOutAnimation.setDuration(600); // Animation duration in milliseconds
                splashText.startAnimation(fadeOutAnimation);

                Intent intent;
                if (isOnboardingCompleted()) {
                    // Onboarding is completed, launch login activity
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                } else {
                    // Onboarding is not completed, launch onboarding activity
                    intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                }
                startActivity(intent);
                finish();
            }
        },4000);


    }

    private boolean isOnboardingCompleted() {
        // Initialize SharedPreferences properly before using it
        sharedPreferences = getSharedPreferences("onboarding", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isCompleted", false);
    }

}