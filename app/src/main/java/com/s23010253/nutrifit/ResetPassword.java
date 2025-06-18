package com.s23010253.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPassword extends AppCompatActivity {
    TextView SignIN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        Button btnGetStarted = findViewById(R.id.VerifyBtn);
        btnGetStarted.setOnClickListener(v ->
                startActivity(new Intent(ResetPassword.this, VerifiyPassword.class))
        );
        SignIN = findViewById(R.id.SignIN);
        SignIN.setOnClickListener(view -> {
            // Create an intent to navigate to CreateAccountActivity
            Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
            startActivity(intent);
        });

    }
}
