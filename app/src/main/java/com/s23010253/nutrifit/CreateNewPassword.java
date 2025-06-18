package com.s23010253.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CreateNewPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);

        Button btnGetStarted = findViewById(R.id.SetBtn);
        btnGetStarted.setOnClickListener(v ->
                startActivity(new Intent(CreateNewPassword.this, LoginActivity.class))
        );
    }
}
