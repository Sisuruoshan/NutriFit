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

    private static final String GOOGLE_CLOUD_VISION_API_KEY = "AIzaSyB0FaPLG9RoxV6IuDKZAawRz68hSn8HIfk";
    private static final String GOOGLE_CLOUD_VISION_URL = "https://vision.googleapis.com/v1/images:annotate?key=" + GOOGLE_CLOUD_VISION_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);

        initializeViews();
        setupClickListeners();
        checkCameraPermission();
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
        new GoogleVisionRecognitionTask().execute(bitmap);
    }

    private class GoogleVisionRecognitionTask extends AsyncTask<Bitmap, Void, String[]> {
        @Override
        protected String[] doInBackground(Bitmap... bitmaps) {
            HttpURLConnection conn = null;
            try {
                Bitmap bitmap = bitmaps[0];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);

                JSONObject image = new JSONObject();
                image.put("content", base64Image);

                JSONObject feature = new JSONObject();
                feature.put("type", "LABEL_DETECTION");
                feature.put("maxResults", 8);

                JSONObject request = new JSONObject();
                request.put("image", image);
                request.put("features", new org.json.JSONArray().put(feature));

                JSONObject postData = new JSONObject();
                postData.put("requests", new org.json.JSONArray().put(request));

                URL url = new URL(GOOGLE_CLOUD_VISION_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "";
                    JSONObject responseJson = new JSONObject(response);
                    JSONArray responses = responseJson.optJSONArray("responses");
                    if (responses != null && responses.length() > 0) {
                        JSONArray labels = responses.getJSONObject(0).optJSONArray("labelAnnotations");
                        if (labels != null && labels.length() > 0) {
                            String[] labelArr = new String[labels.length()];
                            for (int i = 0; i < labels.length(); i++) {
                                labelArr[i] = labels.getJSONObject(i).getString("description");
                            }
                            return labelArr;
                        }
                    }
                } else {
                    // Read error stream and show error message
                    String errorMsg = "";
                    try {
                        java.util.Scanner s = new java.util.Scanner(conn.getErrorStream()).useDelimiter("\\A");
                        errorMsg = s.hasNext() ? s.next() : "";
                    } catch (Exception ex) {}
                    final String msg = "Vision API error: " + responseCode + "\n" + errorMsg;
                    runOnUiThread(() -> Toast.makeText(ScanActivity.this, msg, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String msg = "Exception: " + e.getMessage();
                runOnUiThread(() -> Toast.makeText(ScanActivity.this, msg, Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] labels) {
            if (labels != null && labels.length > 0) {
                // Try to match any label to the nutrition database (singular/plural, case-insensitive)
                for (String label : labels) {
                    String normalized = capitalizeWords(label.trim());
                    if (nutritionDatabaseHasFood(normalized)) {
                        displayResult(normalized, lastCapturedBitmap);
                        return;
                    }
                    // Try singular/plural
                    String singular = toSingular(normalized);
                    String plural = toPlural(normalized);
                    if (nutritionDatabaseHasFood(singular)) {
                        displayResult(singular, lastCapturedBitmap);
                        return;
                    }
                    if (nutritionDatabaseHasFood(plural)) {
                        displayResult(plural, lastCapturedBitmap);
                        return;
                    }
                }
                // If no match, show dialog to pick or enter manually
                showLabelChoiceDialog(labels);
            } else {
                Toast.makeText(ScanActivity.this, "Could not recognize food. Try again.", Toast.LENGTH_SHORT).show();
                proTipText.setText("Recognition failed. Try again or use manual entry.");
            }
        }
    }

    private void showLabelChoiceDialog(String[] labels) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Food or Enter Manually");
        builder.setItems(labels, (dialog, which) -> {
            String chosen = capitalizeWords(labels[which].trim());
            openNutritionAnalysis(chosen, lastCapturedBitmap);
        });
        builder.setNegativeButton("Manual Entry", (dialog, which) -> showManualEntryDialog());
        builder.show();
    }

    private String toSingular(String word) {
        if (word.endsWith("es")) return word.substring(0, word.length() - 2);
        if (word.endsWith("s")) return word.substring(0, word.length() - 1);
        return word;
    }
    private String toPlural(String word) {
        if (!word.endsWith("s")) return word + "s";
        return word;
    }

    private boolean nutritionDatabaseHasFood(String foodName) {
        // Check against the NutritionAnalysisActivity database
        // For simplicity, check a few common variants
        try {
            java.lang.reflect.Field dbField = Class.forName("com.s23010253.nutrifit.NutritionAnalysisActivity").getDeclaredField("nutritionDatabase");
            dbField.setAccessible(true);
            Object db = dbField.get(null);
            if (db instanceof Map) {
                Map map = (Map) db;
                return map.containsKey(foodName);
            }
        } catch (Exception e) {
            // Fallback: try common foods
            String[] foods = {"Apple","Banana","Orange","Grilled Chicken Breast","Bread","Milk","Rice","Pasta","Pizza","Sandwich","Salad","Burger","Fish","Vegetables","Cheese","Yogurt","Eggs","Cereal","Soup","Steak","Chicken","Egg","Beef","Pork","Lamb","Turkey","Duck","Salmon","Tuna","Shrimp","Crab","Lobster","Cod","Tilapia","Sardine","Mackerel","Trout","Ham","Bacon","Sausage","Duck Egg","Quail Egg","Goose Egg","Tofu","Tempeh","Seitan","Paneer","Mozzarella","Parmesan","Cheddar","Feta","Cottage Cheese","Ricotta","Swiss Cheese","Brie","Camembert","Gouda","Edam","Emmental","Blue Cheese","Goat Cheese","Provolone","Manchego","Halloumi","Mascarpone","Cream Cheese"};
            for (String f : foods) {
                if (f.equalsIgnoreCase(foodName)) return true;
            }
        }
        return false;
    }

    private String capitalizeWords(String input) {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    // Overload displayResult to accept bitmap
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
        startActivity(intent);
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
