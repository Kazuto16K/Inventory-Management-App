package com.example.inventoryapp;

/**
 * CONFIGURATION FOR AUTOMATED EMAIL SENDING
 * 
 * IMPORTANT: To use Gmail, you must:
 * 1. Enable 2-Step Verification on your Google Account.
 * 2. Generate an "App Password" (16 characters) and paste it below.
 *    Go to: https://myaccount.google.com/apppasswords
 */
public class SecretConfig {
    
    // 1. Enter your full Gmail address here
    public static final String SENDER_EMAIL = "soumajithdas@gmail.com";
    
    // 2. Enter the 16-character App Password (without spaces)
    public static final String APP_PASSWORD = "fydblqbbzgyrbfkg";
    
    // Global admin passcode if needed for other features
    public static final String ADMIN_OVERRIDE_PASSCODE = "123456";
}
