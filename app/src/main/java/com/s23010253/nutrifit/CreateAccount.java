package com.s23010253.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Button btnGetStarted = findViewById(R.id.continueBtn);
        btnGetStarted.setOnClickListener(v ->
                startActivity(new Intent(CreateAccount.this, LoginActivity.class))
        );
    }
}