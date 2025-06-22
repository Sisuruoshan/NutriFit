package com.s23010253.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SuccessfulActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful);

        Button btnGoToLogin = findViewById(R.id.ContinueTOSIgnUpBtn);
        btnGoToLogin.setOnClickListener(v ->
                startActivity(new Intent(SuccessfulActivity.this, LoginActivity.class))
        );
    }
}
