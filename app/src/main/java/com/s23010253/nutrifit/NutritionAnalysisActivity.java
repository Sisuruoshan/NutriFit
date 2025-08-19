package com.s23010253.nutrifit;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NutritionAnalysisActivity extends AppCompatActivity {

    private TextView foodNameText;
    private TextView confidenceText;
    private EditText servingSizeInput;
    private TextView caloriesValue;
    private TextView proteinValue;
    private TextView fatValue;
    private TextView carbsValue;
    private Button addToDailyLogButton;
    private ImageView foodImageView;

    // Base nutrition data per 100g
    private Map<String, FoodNutrition> nutritionDatabase;
    private String currentFood;
    private int currentServingSize = 100;

    private ImageView navHome, navLocation, navProfile, navScan, navSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_analysis);

        // Enable immersive full screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        initializeViews();
        initializeBottomNavigation();
        initializeNutritionDatabase();
        setupClickListeners();

        // Get the detected food from intent
        Intent intent = getIntent();
        currentFood = intent.getStringExtra("detected_food");
        int confidence = intent.getIntExtra("confidence", 85);

        // Handle captured image
        byte[] imageBytes = intent.getByteArrayExtra("captured_image");
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                foodImageView.setImageBitmap(bitmap);
            }
        } else {
            // If no image, try to set a relevant image based on food name (manual entry)
            String foodImageName = intent.getStringExtra("food_image_name");
            if (foodImageName != null) {
                // Fetch from Unsplash API
                fetchFoodImageFromUnsplash(foodImageName);
            }
        }

        if (currentFood != null) {
            displayFoodAnalysis(currentFood, confidence);
        }
    }

    private void initializeViews() {
        foodNameText = findViewById(R.id.foodNameText);
        confidenceText = findViewById(R.id.confidenceText);
        servingSizeInput = findViewById(R.id.servingSizeInput);
        caloriesValue = findViewById(R.id.caloriesValue);
        proteinValue = findViewById(R.id.proteinValue);
        fatValue = findViewById(R.id.fatValue);
        carbsValue = findViewById(R.id.carbsValue);
        addToDailyLogButton = findViewById(R.id.addToDailyLogButton);
        foodImageView = findViewById(R.id.foodImageView);
    }

    private void initializeBottomNavigation() {
        navHome = findViewById(R.id.navHome);
        navLocation = findViewById(R.id.navLocation);
        navProfile = findViewById(R.id.navProfile);
        navScan = findViewById(R.id.navScan);
        navSetting = findViewById(R.id.navSetting);

        navHome.setOnClickListener(v -> navigateToHome());
        navLocation.setOnClickListener(v -> navigateToLocations());
        navProfile.setOnClickListener(v -> navigateToProfile());
        navScan.setOnClickListener(v -> navigateToScan());
        navSetting.setOnClickListener(v -> navigateToSetting());
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void navigateToLocations() {
        Intent intent = new Intent(this, FitMapActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void navigateToScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void navigateToSetting() {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void initializeNutritionDatabase() {
        nutritionDatabase = new HashMap<>();

        // Sample nutrition data (per 100g)
        nutritionDatabase.put("Apple", new FoodNutrition(52, 0.3, 0.2, 14));
        nutritionDatabase.put("Banana", new FoodNutrition(89, 1.1, 0.3, 23));
        nutritionDatabase.put("Orange", new FoodNutrition(47, 0.9, 0.1, 12));
        nutritionDatabase.put("Grilled Chicken Breast", new FoodNutrition(165, 31, 3.6, 0));
        nutritionDatabase.put("Bread", new FoodNutrition(265, 9, 3.2, 49));
        nutritionDatabase.put("Milk", new FoodNutrition(42, 3.4, 1, 5));
        nutritionDatabase.put("Rice", new FoodNutrition(130, 2.7, 0.3, 28));
        nutritionDatabase.put("Pasta", new FoodNutrition(131, 5, 1.1, 25));
        nutritionDatabase.put("Pizza", new FoodNutrition(266, 11, 10, 33));
        nutritionDatabase.put("Sandwich", new FoodNutrition(250, 12, 8, 30));
        nutritionDatabase.put("Salad", new FoodNutrition(20, 1.5, 0.2, 4));
        nutritionDatabase.put("Burger", new FoodNutrition(295, 17, 14, 28));
        nutritionDatabase.put("Fish", new FoodNutrition(206, 22, 12, 0));
        nutritionDatabase.put("Vegetables", new FoodNutrition(25, 2, 0.3, 5));
        nutritionDatabase.put("Cheese", new FoodNutrition(113, 7, 9, 1));
        nutritionDatabase.put("Yogurt", new FoodNutrition(59, 10, 0.4, 3.6));
        nutritionDatabase.put("Eggs", new FoodNutrition(155, 13, 11, 1.1));
        nutritionDatabase.put("Cereal", new FoodNutrition(379, 8, 6, 84));
        nutritionDatabase.put("Soup", new FoodNutrition(50, 3, 1.5, 7));
        nutritionDatabase.put("Steak", new FoodNutrition(271, 26, 18, 0));
        // Additional foods
        nutritionDatabase.put("Mango", new FoodNutrition(60, 0.8, 0.4, 15));
        nutritionDatabase.put("Avocado", new FoodNutrition(160, 2, 15, 9));
        nutritionDatabase.put("Potato", new FoodNutrition(77, 2, 0.1, 17));
        nutritionDatabase.put("Tomato", new FoodNutrition(18, 0.9, 0.2, 3.9));
        nutritionDatabase.put("Onion", new FoodNutrition(40, 1.1, 0.1, 9));
        nutritionDatabase.put("Carrot", new FoodNutrition(41, 0.9, 0.2, 10));
        nutritionDatabase.put("Broccoli", new FoodNutrition(34, 2.8, 0.4, 7));
        nutritionDatabase.put("Spinach", new FoodNutrition(23, 2.9, 0.4, 3.6));
        nutritionDatabase.put("Lettuce", new FoodNutrition(15, 1.4, 0.2, 2.9));
        nutritionDatabase.put("Pineapple", new FoodNutrition(50, 0.5, 0.1, 13));
        nutritionDatabase.put("Strawberry", new FoodNutrition(32, 0.7, 0.3, 7.7));
        nutritionDatabase.put("Blueberry", new FoodNutrition(57, 0.7, 0.3, 14));
        nutritionDatabase.put("Grapes", new FoodNutrition(69, 0.7, 0.2, 18));
        nutritionDatabase.put("Watermelon", new FoodNutrition(30, 0.6, 0.2, 8));
        nutritionDatabase.put("Pear", new FoodNutrition(57, 0.4, 0.1, 15));
        nutritionDatabase.put("Peach", new FoodNutrition(39, 0.9, 0.3, 10));
        nutritionDatabase.put("Plum", new FoodNutrition(46, 0.7, 0.3, 11));
        nutritionDatabase.put("Apricot", new FoodNutrition(48, 1.4, 0.4, 11));
        nutritionDatabase.put("Kiwi", new FoodNutrition(61, 1.1, 0.5, 15));
        nutritionDatabase.put("Lemon", new FoodNutrition(29, 1.1, 0.3, 9));
        nutritionDatabase.put("Lime", new FoodNutrition(30, 0.7, 0.2, 11));
        nutritionDatabase.put("Coconut", new FoodNutrition(354, 3.3, 33, 15));
        nutritionDatabase.put("Almonds", new FoodNutrition(579, 21, 50, 22));
        nutritionDatabase.put("Walnuts", new FoodNutrition(654, 15, 65, 14));
        nutritionDatabase.put("Peanuts", new FoodNutrition(567, 26, 49, 16));
        nutritionDatabase.put("Cashews", new FoodNutrition(553, 18, 44, 30));
        nutritionDatabase.put("Hazelnuts", new FoodNutrition(628, 15, 61, 17));
        nutritionDatabase.put("Sunflower Seeds", new FoodNutrition(584, 21, 51, 20));
        nutritionDatabase.put("Pumpkin Seeds", new FoodNutrition(446, 19, 19, 54));
        nutritionDatabase.put("Chia Seeds", new FoodNutrition(486, 17, 31, 42));
        nutritionDatabase.put("Quinoa", new FoodNutrition(120, 4.1, 1.9, 21));
        nutritionDatabase.put("Oats", new FoodNutrition(389, 17, 7, 66));
        nutritionDatabase.put("Barley", new FoodNutrition(354, 12, 2.3, 73));
        nutritionDatabase.put("Lentils", new FoodNutrition(116, 9, 0.4, 20));
        nutritionDatabase.put("Chickpeas", new FoodNutrition(164, 9, 2.6, 27));
        nutritionDatabase.put("Kidney Beans", new FoodNutrition(127, 8.7, 0.5, 22));
        nutritionDatabase.put("Black Beans", new FoodNutrition(132, 8.9, 0.5, 24));
        nutritionDatabase.put("Green Peas", new FoodNutrition(81, 5, 0.4, 14));
        nutritionDatabase.put("Chicken", new FoodNutrition(239, 27, 14, 0));
        nutritionDatabase.put("Egg", new FoodNutrition(155, 13, 11, 1.1));
        nutritionDatabase.put("Beef", new FoodNutrition(250, 26, 17, 0));
        nutritionDatabase.put("Pork", new FoodNutrition(242, 27, 14, 0));
        nutritionDatabase.put("Lamb", new FoodNutrition(294, 25, 21, 0));
        nutritionDatabase.put("Turkey", new FoodNutrition(189, 29, 7, 0));
        nutritionDatabase.put("Duck", new FoodNutrition(337, 27, 28, 0));
        nutritionDatabase.put("Salmon", new FoodNutrition(208, 20, 13, 0));
        nutritionDatabase.put("Tuna", new FoodNutrition(132, 28, 1, 0));
        nutritionDatabase.put("Shrimp", new FoodNutrition(99, 24, 0.3, 0.2));
        nutritionDatabase.put("Crab", new FoodNutrition(87, 18, 1, 0));
        nutritionDatabase.put("Lobster", new FoodNutrition(89, 19, 0.9, 0));
        nutritionDatabase.put("Cod", new FoodNutrition(82, 18, 0.7, 0));
        nutritionDatabase.put("Tilapia", new FoodNutrition(96, 20, 2, 0));
        nutritionDatabase.put("Sardine", new FoodNutrition(208, 25, 11, 0));
        nutritionDatabase.put("Mackerel", new FoodNutrition(205, 19, 13, 0));
        nutritionDatabase.put("Trout", new FoodNutrition(119, 20, 4, 0));
        nutritionDatabase.put("Ham", new FoodNutrition(145, 21, 6, 1.5));
        nutritionDatabase.put("Bacon", new FoodNutrition(541, 37, 42, 1.4));
        nutritionDatabase.put("Sausage", new FoodNutrition(301, 12, 27, 1.7));
        nutritionDatabase.put("Duck Egg", new FoodNutrition(185, 13, 14, 1.5));
        nutritionDatabase.put("Quail Egg", new FoodNutrition(158, 13, 11, 0.4));
        nutritionDatabase.put("Goose Egg", new FoodNutrition(185, 14, 13, 1.3));
        nutritionDatabase.put("Tofu", new FoodNutrition(76, 8, 4.8, 1.9));
        nutritionDatabase.put("Tempeh", new FoodNutrition(192, 20, 11, 7.6));
        nutritionDatabase.put("Seitan", new FoodNutrition(370, 75, 1.9, 14));
        nutritionDatabase.put("Paneer", new FoodNutrition(265, 18, 21, 1.2));
        nutritionDatabase.put("Mozzarella", new FoodNutrition(280, 18, 17, 3));
        nutritionDatabase.put("Parmesan", new FoodNutrition(431, 38, 29, 4.1));
        nutritionDatabase.put("Cheddar", new FoodNutrition(403, 25, 33, 1.3));
        nutritionDatabase.put("Feta", new FoodNutrition(264, 14, 21, 4.1));
        nutritionDatabase.put("Cottage Cheese", new FoodNutrition(98, 11, 4.3, 3.4));
        nutritionDatabase.put("Ricotta", new FoodNutrition(174, 11, 13, 3));
        nutritionDatabase.put("Swiss Cheese", new FoodNutrition(380, 27, 28, 5));
        nutritionDatabase.put("Brie", new FoodNutrition(334, 21, 28, 0.5));
        nutritionDatabase.put("Camembert", new FoodNutrition(300, 20, 24, 0.5));
        nutritionDatabase.put("Gouda", new FoodNutrition(356, 25, 27, 2.2));
        nutritionDatabase.put("Edam", new FoodNutrition(357, 25, 27, 1.4));
        nutritionDatabase.put("Emmental", new FoodNutrition(380, 28, 30, 0));
        nutritionDatabase.put("Blue Cheese", new FoodNutrition(353, 21, 29, 2.3));
        nutritionDatabase.put("Goat Cheese", new FoodNutrition(364, 22, 30, 2.2));
        nutritionDatabase.put("Provolone", new FoodNutrition(351, 26, 27, 2.1));
        nutritionDatabase.put("Manchego", new FoodNutrition(393, 25, 33, 1.6));
        nutritionDatabase.put("Halloumi", new FoodNutrition(321, 22, 26, 2.2));
        nutritionDatabase.put("Mascarpone", new FoodNutrition(429, 7, 44, 4));
        nutritionDatabase.put("Cream Cheese", new FoodNutrition(342, 6, 34, 4));
        nutritionDatabase.put("Roast Chicken", new FoodNutrition(177, 27.3, 7.7, 0)); // per 100g
    }

    private void setupClickListeners() {
        addToDailyLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToDailyLog();
            }
        });

        servingSizeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    updateNutritionValues(currentFood);
                }
            }
        });
        servingSizeInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateNutritionValues(currentFood);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void displayFoodAnalysis(String foodName, int confidence) {
        foodNameText.setText(foodName);
        confidenceText.setText(confidence + "% confidence match");

        // Set food image placeholder
        // Removed setFoodImage call

        // Display nutrition information
        updateNutritionValues(foodName);
    }

    private void updateNutritionValues(String foodName) {
        try {
            String servingSizeText = servingSizeInput.getText().toString().replace("g", "").trim();
            if (!servingSizeText.isEmpty()) {
                currentServingSize = Integer.parseInt(servingSizeText);
            }
        } catch (NumberFormatException e) {
            currentServingSize = 100;
            servingSizeInput.setText("100g");
        }

        // Make food name lookup case-insensitive
        FoodNutrition nutrition = null;
        for (String key : nutritionDatabase.keySet()) {
            if (key.equalsIgnoreCase(foodName)) {
                nutrition = nutritionDatabase.get(key);
                break;
            }
        }
        if (nutrition != null) {
            // Calculate nutrition based on serving size
            double multiplier = currentServingSize / 100.0;

            int calories = (int) (nutrition.calories * multiplier);
            double protein = nutrition.protein * multiplier;
            double fat = nutrition.fat * multiplier;
            double carbs = nutrition.carbs * multiplier;

            caloriesValue.setText(String.valueOf(calories));
            proteinValue.setText(String.format("%.1fg", protein));
            fatValue.setText(String.format("%.1fg", fat));
            carbsValue.setText(String.format("%.1fg", carbs));
        } else {
            // Default values if food not in database
            caloriesValue.setText("N/A");
            proteinValue.setText("N/A");
            fatValue.setText("N/A");
            carbsValue.setText("N/A");
        }
    }

    private void addToDailyLog() {
        // Simulate adding to daily log
        Toast.makeText(this, currentFood + " added to your daily log!", Toast.LENGTH_SHORT).show();

        // Save calories to SharedPreferences (accumulate for today)
        int caloriesToAdd = 0;
        try {
            caloriesToAdd = Integer.parseInt(caloriesValue.getText().toString());
        } catch (Exception ignored) {}
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        android.content.SharedPreferences prefs = getSharedPreferences("daily_log", MODE_PRIVATE);
        int totalCalories = prefs.getInt("calories_" + today, 0);
        totalCalories += caloriesToAdd;
        prefs.edit().putInt("calories_" + today, totalCalories).apply();

        // In a real app, you would save this to a database or send to a server
        // For now, we'll just go back to the main screen
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NutritionAnalysisActivity.this, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // Inner class to represent food nutrition data
    private static class FoodNutrition {
        int calories;
        double protein;
        double fat;
        double carbs;

        FoodNutrition(int calories, double protein, double fat, double carbs) {
            this.calories = calories;
            this.protein = protein;
            this.fat = fat;
            this.carbs = carbs;
        }
    }

    // Add this helper method at the end of the class
    private int getFoodImageResId(String foodName) {
        // No food images available, always return 0
        return 0;
    }

    // Fetch food image from Unsplash API
    private void fetchFoodImageFromUnsplash(String foodName) {
        final String ACCESS_KEY = "ZQpNGEfSLswbi9wyt1qcalWaLyPdTgsnyexdW3hZUoQ"; // Unsplash Access Key
        final String url = "https://api.unsplash.com/search/photos?query=" + foodName + "&client_id=" + ACCESS_KEY + "&orientation=squarish&per_page=1";
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        InputStream is = connection.getInputStream();
                        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                        String response = s.hasNext() ? s.next() : "";
                        JSONObject json = new JSONObject(response);
                        JSONArray results = json.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            JSONObject first = results.getJSONObject(0);
                            JSONObject urls = first.getJSONObject("urls");
                            return urls.getString("regular");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(String imageUrl) {
                if (imageUrl != null) {
                    new DownloadImageTask(foodImageView).execute(imageUrl);
                }
            }
        }.execute();
    }

    // Helper AsyncTask to download image from URL
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                return BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}
