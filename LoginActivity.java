package com.smartglove.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity
 * First screen the user sees.
 * For this project a simple hardcoded check is used.
 * You can replace with Firebase Auth or SharedPreferences later.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button   btnLogin;
    private TextView tvRegister;

    // Hardcoded demo credentials — change as needed
    private static final String DEMO_EMAIL    = "user@smartglove.com";
    private static final String DEMO_PASSWORD = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email    = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Enter email");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Enter password");
                    return;
                }

                // Simple credential check
                if (email.equals(DEMO_EMAIL) && password.equals(DEMO_PASSWORD)) {
                    Toast.makeText(LoginActivity.this,
                            "Login successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Invalid credentials. Use " + DEMO_EMAIL + " / " + DEMO_PASSWORD,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,
                        "Registration: Use demo credentials for now.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
