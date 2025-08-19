package com.s23010253.nutrifit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileSetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Enable immersive full screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        LinearLayout llFitnessGoal = findViewById(R.id.llFitnessGoal);
        TextView tvFitnessGoal = findViewById(R.id.tvFitnessGoal);
        LinearLayout llActivityLevel = findViewById(R.id.llActivityLevel);
        TextView tvActivityLevel = findViewById(R.id.tvActivityLevel);
        EditText etWeight = findViewById(R.id.etWeight);
        EditText etHeight = findViewById(R.id.etHeight);
        TextView tvProteinValue = findViewById(R.id.tvProteinValue);
        TextView tvCaloriesValue = findViewById(R.id.tvCaloriesValue);
        Button btnSetUp = findViewById(R.id.btnSetUp);

        btnSetUp.setOnClickListener(v -> {
            // Get user input
            String weightStr = etWeight.getText().toString();
            String heightStr = etHeight.getText().toString();
            String goal = tvFitnessGoal.getText().toString();
            String activity = tvActivityLevel.getText().toString();

            double weight = weightStr.isEmpty() ? 0 : Double.parseDouble(weightStr);
            double height = heightStr.isEmpty() ? 0 : Double.parseDouble(heightStr);

            // Calculate BMI
            double bmi = 0;
            if (height > 0) {
                bmi = weight / Math.pow(height / 100.0, 2);
            }

            // Calculate calories
            double bmr = 10 * weight + 6.25 * height - 5 * 25 + 5;
            double activityFactor = 1.2;
            switch (activity) {
                case "Lightly Active": activityFactor = 1.375; break;
                case "Moderately Active": activityFactor = 1.55; break;
                case "Very Active": activityFactor = 1.725; break;
            }
            double calories = bmr * activityFactor;
            if (goal.equals("Muscle Gain")) calories += 300;
            else if (goal.equals("Lose Weight")) calories -= 300;

            // Calculate protein goal
            double proteinPerKg = 2.0;
            if (goal.equals("Maintain Weight")) proteinPerKg = 1.6;
            else if (goal.equals("Lose Weight")) proteinPerKg = 1.8;
            int proteinGoal = (int)Math.round(weight * proteinPerKg);

            // Calculate water goal (simple: 35ml per kg)
            double waterGoal = weight * 0.035; // in liters

            // Pass all values to ProfileActivity
            Intent intent = new Intent(ProfileSetupActivity.this, ProfileActivity.class);
            intent.putExtra("weight", weight);
            intent.putExtra("height", height);
            intent.putExtra("bmi", bmi);
            intent.putExtra("proteinGoal", proteinGoal);
            intent.putExtra("calorieGoal", (int)calories);
            intent.putExtra("waterGoal", waterGoal);
            intent.putExtra("goal", goal);
            intent.putExtra("activity", activity);
            startActivity(intent);
            finish();
        });

        View.OnClickListener updateAndShowDialog = v -> {
            if (v.getId() == R.id.llFitnessGoal) {
                final String[] fitnessGoals = {"Muscle Gain", "Maintain Weight", "Lose Weight"};
                new AlertDialog.Builder(ProfileSetupActivity.this)
                        .setTitle("Select Fitness Goal")
                        .setItems(fitnessGoals, (dialog, which) -> {
                            tvFitnessGoal.setText(fitnessGoals[which]);
                            updateTargets(etWeight, etHeight, tvFitnessGoal, tvActivityLevel, tvProteinValue, tvCaloriesValue);
                        })
                        .show();
            } else if (v.getId() == R.id.llActivityLevel) {
                final String[] activityLevels = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active"};
                new AlertDialog.Builder(ProfileSetupActivity.this)
                        .setTitle("Select Activity Level")
                        .setItems(activityLevels, (dialog, which) -> {
                            tvActivityLevel.setText(activityLevels[which]);
                            updateTargets(etWeight, etHeight, tvFitnessGoal, tvActivityLevel, tvProteinValue, tvCaloriesValue);
                        })
                        .show();
            }
        };
        llFitnessGoal.setOnClickListener(updateAndShowDialog);
        llActivityLevel.setOnClickListener(updateAndShowDialog);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTargets(etWeight, etHeight, tvFitnessGoal, tvActivityLevel, tvProteinValue, tvCaloriesValue);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etWeight.addTextChangedListener(watcher);
        etHeight.addTextChangedListener(watcher);

        // Initial calculation
        updateTargets(etWeight, etHeight, tvFitnessGoal, tvActivityLevel, tvProteinValue, tvCaloriesValue);
        setupBottomNavigation();
    }

    private void updateTargets(EditText etWeight, EditText etHeight, TextView tvFitnessGoal, TextView tvActivityLevel, TextView tvProteinValue, TextView tvCaloriesValue) {
        String weightStr = etWeight.getText().toString();
        String heightStr = etHeight.getText().toString();
        String goal = tvFitnessGoal.getText().toString();
        String activity = tvActivityLevel.getText().toString();
        if (weightStr.isEmpty() || heightStr.isEmpty()) return;
        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);

        // Harris-Benedict BMR (Mifflin-St Jeor, male, age 25 as default)
        double bmr = 10 * weight + 6.25 * height - 5 * 25 + 5;
        double activityFactor = 1.2;
        switch (activity) {
            case "Lightly Active": activityFactor = 1.375; break;
            case "Moderately Active": activityFactor = 1.55; break;
            case "Very Active": activityFactor = 1.725; break;
        }
        double calories = bmr * activityFactor;
        if (goal.equals("Muscle Gain")) calories += 300;
        else if (goal.equals("Lose Weight")) calories -= 300;

        // Protein: 2.0g/kg for muscle gain, 1.6g/kg for maintain, 1.8g/kg for lose
        double proteinPerKg = 2.0;
        if (goal.equals("Maintain Weight")) proteinPerKg = 1.6;
        else if (goal.equals("Lose Weight")) proteinPerKg = 1.8;
        int protein = (int)Math.round(weight * proteinPerKg);

        tvProteinValue.setText(protein + "g");
        tvCaloriesValue.setText(String.format("%,.0f", calories));
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

        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(this, ScanActivity.class));
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }
}
