package com.s23010253.nutrifit;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private TextView usernameTextView;
    private static final String PREFS_NAME = "profile_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROFILE_IMAGE = "profile_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Enable immersive full screen mode for all devices
        setImmersiveMode();

        // Get data from intent
        double weight = getIntent().getDoubleExtra("weight", -1);
        double height = getIntent().getDoubleExtra("height", -1);
        double bmi = getIntent().getDoubleExtra("bmi", -1);
        int proteinGoal = getIntent().getIntExtra("proteinGoal", -1);
        int calorieGoal = getIntent().getIntExtra("calorieGoal", -1);
        double waterGoal = getIntent().getDoubleExtra("waterGoal", -1);
        String goal = getIntent().getStringExtra("goal");
        String activity = getIntent().getStringExtra("activity");

        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        ImageView editUsernameIcon = findViewById(R.id.editUsernameIcon);
        ImageView editProfileImageIcon = findViewById(R.id.editProfileImageIcon);

        // Load saved username and profile image
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "User Name");
        usernameTextView.setText(savedUsername);
        String imageBase64 = prefs.getString(KEY_PROFILE_IMAGE, null);
        if (imageBase64 != null) {
            try {
                byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                profileImageView.setImageBitmap(bitmap);
            } catch (Exception ignored) {}
        }

        // Persist profile details (weight, height, bmi, proteinGoal, calorieGoal, waterGoal, goal, activity)
        SharedPreferences.Editor editor = prefs.edit();
        if (weight != -1) editor.putFloat("weight", (float)weight);
        if (height != -1) editor.putFloat("height", (float)height);
        if (bmi != -1) editor.putFloat("bmi", (float)bmi);
        if (proteinGoal != -1) editor.putInt("proteinGoal", proteinGoal);
        if (calorieGoal != -1) editor.putInt("calorieGoal", calorieGoal);
        if (waterGoal != -1) editor.putFloat("waterGoal", (float)waterGoal);
        if (goal != null) editor.putString("goal", goal);
        if (activity != null) editor.putString("activity", activity);
        editor.apply();

        // Load profile details from SharedPreferences if not provided by intent
        if (weight == -1) weight = prefs.getFloat("weight", 0);
        if (height == -1) height = prefs.getFloat("height", 0);
        if (bmi == -1) bmi = prefs.getFloat("bmi", 0);
        if (proteinGoal == -1) proteinGoal = prefs.getInt("proteinGoal", 0);
        if (calorieGoal == -1) calorieGoal = prefs.getInt("calorieGoal", 0);
        if (waterGoal == -1) waterGoal = prefs.getFloat("waterGoal", 0);
        if (goal == null) goal = prefs.getString("goal", "Goal: Muscle Gain");
        if (activity == null) activity = prefs.getString("activity", "Moderately Active");

        // Set values in UI
        TextView weightText = findViewById(R.id.weightTextView);
        if (weightText != null) weightText.setText(String.format("%.0fkg", weight));
        TextView heightText = findViewById(R.id.heightTextView);
        if (heightText != null) heightText.setText(String.format("%.0fcm", height));
        TextView bmiText = findViewById(R.id.bmiTextView);
        TextView bmiStatusText = null;
        if (bmiText != null) bmiText.setText(String.format("%.1f", bmi));
        // Set BMI status (Healthy/Unhealthy)
        if (bmiText != null) {
            bmiStatusText = findViewById(R.id.bmiStatusTextView);
            double bmiValue = bmi;
            String status;
            int color;
            if (bmiValue >= 18.5 && bmiValue <= 24.9) {
                status = "Healthy";
                color = getResources().getColor(android.R.color.holo_green_light);
            } else {
                status = "Unhealthy";
                color = getResources().getColor(android.R.color.holo_red_light);
            }
            if (bmiStatusText != null) {
                bmiStatusText.setText(status);
                bmiStatusText.setTextColor(color);
            }
        }
        TextView proteinGoalText = findViewById(R.id.proteinGoalTextView);
        if (proteinGoalText != null) proteinGoalText.setText(proteinGoal + "g");
        TextView calorieGoalText = findViewById(R.id.calorieGoalTextView);
        if (calorieGoalText != null) calorieGoalText.setText(String.format("%d", calorieGoal));
        TextView waterGoalText = findViewById(R.id.waterGoalTextView);
        if (waterGoalText != null) waterGoalText.setText(String.format("%.1fL", waterGoal));
        TextView goalText = findViewById(R.id.goalTextView);
        if (goalText != null && goal != null) goalText.setText(goal);
        TextView activityText = findViewById(R.id.activityTextView);
        if (activityText != null && activity != null) activityText.setText(activity);


        // Profile picture change (icon or image click)
        View.OnClickListener imagePickerListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        };
        profileImageView.setOnClickListener(imagePickerListener);
        editProfileImageIcon.setOnClickListener(imagePickerListener);

        // Username change
        editUsernameIcon.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setText(usernameTextView.getText());
            new AlertDialog.Builder(this)
                .setTitle("Edit Username")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        usernameTextView.setText(newName);
                        prefs.edit().putString(KEY_USERNAME, newName).apply();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        // Logout
        findViewById(R.id.logoutIcon).setOnClickListener(v -> performLogout());
        findViewById(R.id.logoutText).setOnClickListener(v -> performLogout());

        // Bottom navigation setup
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(this, FitMapActivity.class));
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(this, ScanActivity.class));
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileSetupActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
                // Save image to SharedPreferences as Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                String imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_PROFILE_IMAGE, imageBase64).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveMode();
        }
    }

    private void performLogout() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}