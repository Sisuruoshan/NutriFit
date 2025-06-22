package com.s23010253.nutrifit;

import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Header
        userName = findViewById(R.id.userName);  // Make sure your TextView has this ID
        userName.setText("Hi, User Name"); // Set dynamically if needed

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
            Toast.makeText(this, "Opening Full Body Workout...", Toast.LENGTH_SHORT).show();
            // You can start a new activity here
            // startActivity(new Intent(this, FullBodyWorkoutActivity.class));
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
            startActivity(new Intent(HomeActivity.this, ProfileSetupActivity.class));
        });
        navScan.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, ScanActivity.class));
        });
        //navSetting.setOnClickListener(view -> {
        //  startActivity(new Intent(this, SettingActivity.class));
        //}
        //);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            steps.setText(String.valueOf(stepsValue));
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
}
