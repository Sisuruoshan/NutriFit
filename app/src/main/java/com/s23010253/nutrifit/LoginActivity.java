package com.s23010253.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText emailField, passwordField;
    Button loginBtn;
    TextView createAccount;
    TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailField = findViewById(R.id.resetEmailField);
        passwordField = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.VerifyBtn);

        loginBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            Toast.makeText(this, "Login clicked: " + email, Toast.LENGTH_SHORT).show();
        });
        createAccount = findViewById(R.id.createAccount);
        createAccount.setOnClickListener(view -> {
            // Create an intent to navigate to CreateAccountActivity
            Intent intent = new Intent(LoginActivity.this, CreateAccount.class);
            startActivity(intent);
        });
        forgotPassword = findViewById(R.id.countDown);
        forgotPassword.setOnClickListener(view -> {
            // Create an intent to navigate to CreateAccountActivity
            Intent intent = new Intent(LoginActivity.this, ResetPassword.class);
            startActivity(intent);
        });

    }
}