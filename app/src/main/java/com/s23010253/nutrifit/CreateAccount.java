package com.s23010253.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Enable immersive full screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        EditText emailField = findViewById(R.id.resetEmailField1);
        EditText usernameField = findViewById(R.id.username);
        EditText ageField = findViewById(R.id.ageField);
        EditText genderField = findViewById(R.id.genderField);
        EditText passwordField = findViewById(R.id.passwordField);
        Button btnGetStarted = findViewById(R.id.continueBtn);
        btnGetStarted.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String username = usernameField.getText().toString().trim();
            String age = ageField.getText().toString().trim();
            String gender = genderField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            if (email.isEmpty() || username.isEmpty() || age.isEmpty() || gender.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);
            boolean success = dbHelper.registerUser(email, password);
            if (success) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreateAccount.this, LoginActivity.class));
            } else {
                Toast.makeText(this, "Account creation failed. Email may already exist.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}