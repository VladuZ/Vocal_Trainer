package com.example.testing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityRegister extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Check if token exists
        if (isTokenPresent()) {
            // If token exists, navigate to MainActivity
            Intent intent = new Intent(ActivityRegister.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish this activity so the user cannot go back to it
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister.setOnClickListener(v -> registerUser());

        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
            startActivity(intent);
        });
    }

    private boolean isTokenPresent() {
        String filename = "user_data.txt";
        try (FileInputStream fis = openFileInput(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Token:")) {
                    String token = line.substring("Token:".length()).trim();
                    if (token != null && !token.isEmpty()) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            Toast.makeText(ActivityRegister.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 10 || password.length() > 50) {
            Toast.makeText(ActivityRegister.this, "Password must be between 10 and 50 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(ActivityRegister.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(username, password, email);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<User> call = apiService.registerUser(user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActivityRegister.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    navigateToHomeFragment(username, email, "sample_token");
                } else {
                    Toast.makeText(ActivityRegister.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ActivityRegister.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHomeFragment(String username, String email, String token) {
        ActivityLogin.updateUserData(username, token, email, this);

        // Navigate to the MainActivity
        Intent intent = new Intent(ActivityRegister.this, MainActivity.class);
        startActivity(intent);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
