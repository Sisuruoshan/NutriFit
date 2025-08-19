package com.s23010253.nutrifit;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private Button scanFoodButton;
    private Button captureImageButton;
    private Button manualEntryButton;
    private ImageView scannerFrame;
    private TextView proTipText;
    private TextView resultText;

    // Mock food database for demonstration
    private List<String> foodItems = Arrays.asList(
            "Apple", "Banana", "Orange", "Bread", "Milk", "Chicken", "Rice",
            "Pasta", "Pizza", "Sandwich", "Salad", "Burger", "Fish", "Vegetables",
            "Cheese", "Yogurt", "Eggs", "Cereal", "Soup", "Steak"
    );

    private Bitmap lastCapturedBitmap; // Store last captured image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);

        // Enable immersive full screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        initializeViews();
        setupClickListeners();
        checkCameraPermission();
        setupBottomNavigation();
    }

    private void initializeViews() {
        scanFoodButton = findViewById(R.id.scanFoodButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        manualEntryButton = findViewById(R.id.manualEntryButton);
        scannerFrame = findViewById(R.id.scannerFrame);
        proTipText = findViewById(R.id.proTipText);
        resultText = findViewById(R.id.resultText); // Ensure resultText is initialized
    }

    private void setupClickListeners() {
        scanFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraScanning();
            }
        });

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        manualEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManualEntryDialog();
            }
        });
    }

    private void showManualEntryDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Manual Food Entry");
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter food name");
        builder.setView(input);
        builder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                String foodName = input.getText().toString().trim();
                if (!foodName.isEmpty()) {
                    openNutritionAnalysis(foodName);
                } else {
                    Toast.makeText(ScanActivity.this, "Please enter a food name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void startCameraScanning() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Simulate scanning process
            scannerFrame.setVisibility(View.VISIBLE);
            proTipText.setText("Scanning... Point camera at food item");

            // Simulate processing delay
            scannerFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openCamera();
                }
            }, 1500);
        } else {
            Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_SHORT).show();
            checkCameraPermission();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    lastCapturedBitmap = imageBitmap; // Save for later use
                    processImage(imageBitmap);
                }
            }
        }
    }

    private void processImage(Bitmap bitmap) {
        proTipText.setText("Analyzing image...");
        // Randomly select a food name from the mock database
        String randomFood = foodItems.get(new Random().nextInt(foodItems.size()));
        displayResult(randomFood, bitmap);
    }

    private void displayResult(String foodName, Bitmap imageBitmap) {
        resultText.setVisibility(View.VISIBLE);
        resultText.setText("Detected: " + foodName);
        proTipText.setText("Food identified! Opening nutrition analysis...");
        openNutritionAnalysis(foodName, imageBitmap);
    }

    // Keep the old displayResult for compatibility (if needed elsewhere)
    private void displayResult(String foodName) {
        displayResult(foodName, lastCapturedBitmap);
    }

    private void openNutritionAnalysis(String foodName, Bitmap imageBitmap) {
        Intent intent = new Intent(this, NutritionAnalysisActivity.class);
        intent.putExtra("detected_food", foodName);
        intent.putExtra("confidence", 85 + new Random().nextInt(15)); // Random confidence 85-99%
        // Convert Bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        intent.putExtra("captured_image", byteArray);
        startActivity(intent);
    }
    private void openNutritionAnalysis(String foodName) {
        Intent intent = new Intent(this, NutritionAnalysisActivity.class);
        intent.putExtra("detected_food", foodName);
        intent.putExtra("confidence", 85 + new Random().nextInt(15)); // Random confidence 85-99%
        // Pass a food image resource name as extra for manual entry
        intent.putExtra("food_image_name", foodName);
        startActivity(intent);
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
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileSetupActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied. Scanner won't work without it.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
