package com.example.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    private final String[] emojis = {"📦", "📊", "🛒", "✅", "🚀"};
    private int currentEmojiIndex = 0;
    private TextView tvEmoji;
    private final Handler emojiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SessionManager sessionManager = new SessionManager(this);
        AppCompatDelegate.setDefaultNightMode(sessionManager.getNightMode());
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tvEmoji = findViewById(R.id.tvSplashEmoji);
        
        // Initialize with the first emoji
        tvEmoji.setText(emojis[currentEmojiIndex]);
        currentEmojiIndex = (currentEmojiIndex + 1) % emojis.length;

        startEmojiAnimation();

        // Delay for 3.5 seconds to navigate, allowing users to see the emoji sequence
        new Handler().postDelayed(() -> {
            emojiHandler.removeCallbacksAndMessages(null);
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 3500);
    }

    private void startEmojiAnimation() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Smooth fade out of the current emoji
                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setDuration(400);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Change to the next emoji in the sequence
                        tvEmoji.setText(emojis[currentEmojiIndex]);
                        currentEmojiIndex = (currentEmojiIndex + 1) % emojis.length;
                        
                        // Smooth fade in of the new emoji
                        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                        fadeIn.setDuration(400);
                        tvEmoji.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                
                tvEmoji.startAnimation(fadeOut);
                
                // Schedule the next transition
                emojiHandler.postDelayed(this, 1000);
            }
        };
        // Start the transition loop after a short initial delay
        emojiHandler.postDelayed(runnable, 1000);
    }
}