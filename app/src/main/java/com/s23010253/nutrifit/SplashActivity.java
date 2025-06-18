package com.s23010253.nutrifit;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show splash screen for a brief moment, then move to main
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
