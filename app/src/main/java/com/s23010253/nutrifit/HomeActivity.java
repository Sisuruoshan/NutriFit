package com.s23010253.nutrifit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    TextView userName, calories, steps, heartBeat, workout;
    ImageView workoutImage;

    ImageView navHome, navLocation, navProfile, navScan, navSetting;
    ImageView profileIcon;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Sensor heartRateSensor;
    private boolean isStepSensorAvailable = false;
    private boolean isHeartRateSensorAvailable = false;
    private float totalSteps = 0;
    private float currentHeartRate = 0;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "stepPrefs";
    private static final String KEY_STEP_OFFSET = "stepOffset";
    private static final String KEY_STEP_DATE = "stepDate";
    private float stepOffset = 0;
    private String todayDate;

    TextView streaksText, currentDayText, upcomingDaysText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Enable immersive full screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Header
        userName = findViewById(R.id.userName);
        // Load username from SharedPreferences (profile_prefs)
        SharedPreferences profilePrefs = getSharedPreferences("profile_prefs", MODE_PRIVATE);
        String savedUsername = profilePrefs.getString("username", "User Name");
        userName.setText("Hi, " + savedUsername); // Set dynamically if needed

        // Profile icon in header
        profileIcon = findViewById(R.id.profileIcon); // Add this ImageView in your layout
        loadProfileImage();
        // Navigate to ProfileActivity when profile icon is clicked
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Statistics
        calories = findViewById(R.id.calories);   // e.g., in layout: android:id="@+id/caloriesText"
        steps = findViewById(R.id.steps);
        heartBeat = findViewById(R.id.heartBeat);
        workout = findViewById(R.id.workout);

        calories.setText("1200 Kcal");
        steps.setText("Waiting..."); // Show waiting until sensor event arrives
        heartBeat.setText("73 bpm");
        workout.setText("14 / 20");

        // Workout preview
        workoutImage = findViewById(R.id.workoutImage);  // e.g., android:id="@+id/workoutImage"
        workoutImage.setOnClickListener(view -> {
            // Open YouTube video in the YouTube app or browser
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://youtu.be/B12MXF0bSFo?si=YAl9sMlkJqS1R8fO"));
            intent.setPackage("com.google.android.youtube");
            // Try to open in YouTube app first
            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                // Fallback to browser if YouTube app is not available
                intent.setPackage(null);
                startActivity(intent);
            }
        });

        // Bottom Navigation
        navHome = findViewById(R.id.navHome);
        navLocation = findViewById(R.id.navLocation);
        navProfile = findViewById(R.id.navProfile);
        navScan = findViewById(R.id.navScan);
        navSetting = findViewById(R.id.navSetting);

        navHome.setOnClickListener(view -> Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show());
        navLocation.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, FitMapActivity.class));
        });
        navProfile.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });
        navScan.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, ScanActivity.class));
        });
        navSetting.setOnClickListener(view -> {
            startActivity(new Intent(this, ProfileSetupActivity.class));
        }
        );
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isStepSensorAvailable = true;
        } else {
            steps.setText("Sensor NA");
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            isHeartRateSensorAvailable = true;
        } else {
            heartBeat.setText("Sensor NA");
        }
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String savedDate = prefs.getString(KEY_STEP_DATE, "");
        if (!todayDate.equals(savedDate)) {
            // New day, reset offset when sensor event arrives
            stepOffset = -1;
        } else {
            stepOffset = prefs.getFloat(KEY_STEP_OFFSET, 0);
        }

        // Day Indicator
        streaksText = findViewById(R.id.streaksText);
        currentDayText = findViewById(R.id.currentDayText);
        upcomingDaysText = findViewById(R.id.upcomingDaysText);
        updateDayBar();
    }

    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE);
        String imageBase64 = prefs.getString("profile_image", null);
        if (imageBase64 != null && profileIcon != null) {
            try {
                byte[] imageBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                // Crop bitmap to circle
                bitmap = getCircularBitmap(bitmap);
                profileIcon.setImageBitmap(bitmap);
            } catch (Exception ignored) {}
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        final int color = 0xff424242;
        final android.graphics.Paint paint = new android.graphics.Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, size, size);
        final android.graphics.RectF rectF = new android.graphics.RectF(rect);
        float radius = size / 2f;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (size - bitmap.getWidth()) / 2f, (size - bitmap.getHeight()) / 2f, paint);
        return output;
    }

    private void updateDayBar() {
        // Get current day and date
        Date today = new Date();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        String dayName = dayFormat.format(today); // e.g., Wed
        String dayNum = dateFormat.format(today); // e.g., 24
        // Streaks (placeholder, can be loaded from SharedPreferences)
        int streaks = 21; // TODO: Load actual streaks
        streaksText.setText(streaks + "\nStreaks");
        // Current day display
        currentDayText.setText(dayName + "\nTime to workout");
        // Upcoming days (next 3 days)
        StringBuilder upcoming = new StringBuilder();
        StringBuilder upcomingNames = new StringBuilder();
        for (int i = 1; i <= 3; i++) {
            Date nextDay = new Date(today.getTime() + i * 24 * 60 * 60 * 1000);
            upcoming.append(new SimpleDateFormat("dd", Locale.getDefault()).format(nextDay));
            upcoming.append(i < 3 ? "    " : "  ");
            upcomingNames.append(new SimpleDateFormat("EEE", Locale.getDefault()).format(nextDay));
            upcomingNames.append(i < 3 ? "   " : "  ");
        }
        upcomingDaysText.setText(upcoming.toString() + "\n" + upcomingNames.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage(); // Refresh profile image in case it was changed
        // Update calories from daily log
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        SharedPreferences dailyLogPrefs = getSharedPreferences("daily_log", MODE_PRIVATE);
        int totalCalories = dailyLogPrefs.getInt("calories_" + today, 0);
        calories.setText(totalCalories + " Kcal");
        if (isStepSensorAvailable) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (isHeartRateSensorAvailable) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int stepsValue = (int) event.values[0];
            // Handle daily step calculation
            if (stepOffset == -1) {
                // First event of the day, set offset
                stepOffset = stepsValue;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(KEY_STEP_OFFSET, stepOffset);
                editor.putString(KEY_STEP_DATE, todayDate);
                editor.apply();
            }
            int stepsToday = (int) (stepsValue - stepOffset);
            if (stepsToday < 0) stepsToday = 0; // Safety check
            steps.setText(String.valueOf(stepsToday));
        }
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            currentHeartRate = event.values[0];
            heartBeat.setText(String.format("%.0f bpm", currentHeartRate));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
