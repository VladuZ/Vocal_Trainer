package com.example.testing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity {
    private EditText etLoginEmail, etLoginPassword;
    private Button btnLogin, btnGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Перехід на екран реєстрації
                Intent intent = new Intent(ActivityLogin.this, ActivityRegister.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            Toast.makeText(ActivityLogin.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 10 || password.length() > 50) {
            Toast.makeText(ActivityLogin.this, "Password must be between 10 and 50 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User("", password, email);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(user);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Toast.makeText(ActivityLogin.this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToHomeFragment(loginResponse.getUsername(), loginResponse.getEmail(), loginResponse.getToken());
                } else {
                    Toast.makeText(ActivityLogin.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(ActivityLogin.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHomeFragment(String username, String email, String token) {
        updateUserData(username, token, email, this);

        // Navigate to the MainActivity
        Intent intent = new Intent(ActivityLogin.this, MainActivity.class);
        startActivity(intent);
    }

    public static void updateUserData(String username, String token, String email, Activity activity) {
        String userData = "Username: " + username + "\nEmail: " + email + "\nToken: " + token;
        String filename = "user_data.txt";
        FragmentAccount.setToken(token);
        try (FileOutputStream fos = activity.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(userData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
