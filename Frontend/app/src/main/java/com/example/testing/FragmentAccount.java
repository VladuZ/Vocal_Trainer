package com.example.testing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAccount extends Fragment {

    String username = null;
    String email = null;
    private static String token = "";

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        FragmentAccount.token = token;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);

        readValues(); // Make sure to call this method to read the values from the file

        usernameTextView.setText("Username: " + username);
        emailTextView.setText("Email: " + email);

        Button changeUsernameButton = view.findViewById(R.id.changeUsernameButton);
        Button changeEmailButton = view.findViewById(R.id.changeEmailButton);
        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);
        Button logOffButton = view.findViewById(R.id.logOffButton);
        Button deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        changeUsernameButton.setOnClickListener(v -> {
            FragmentChangeUsername fragment = new FragmentChangeUsername();
            Bundle args = new Bundle();
            args.putString("oldEmail", email);
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        changeEmailButton.setOnClickListener(v -> {
            FragmentChangeEmail fragment = new FragmentChangeEmail();
            Bundle args = new Bundle();
            args.putString("oldUsername", username);
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        changePasswordButton.setOnClickListener(v -> {
            FragmentChangePassword fragment = new FragmentChangePassword();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        logOffButton.setOnClickListener(v -> logOff());

        deleteAccountButton.setOnClickListener(v -> {
            deleteAccount();
        });

        return view;
    }


    private void readValues() {
        // Read the data from the file
        String filename = "user_data.txt";
        try (FileInputStream fis = getActivity().openFileInput(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Parse each line to extract the values
                if (line.startsWith("Username:")) {
                    username = line.substring("Username:".length()).trim();
                } else if (line.startsWith("Email:")) {
                    email = line.substring("Email:".length()).trim();
                } else if (line.startsWith("Token:")) {
                    token = line.substring("Token:".length()).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readToken(FragmentActivity activity) {
        // Read the data from the file
        String filename = "user_data.txt";
        try (FileInputStream fis = activity.openFileInput(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Token:")) {
                    return line.substring("Token:".length()).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void logOff() {
        // Clear the user_data.txt file
        String filename = "user_data.txt";
        try (FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write("".getBytes()); // Write an empty string to clear the file
            //Toast.makeText(getActivity(), "Logged off successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error logging off", Toast.LENGTH_SHORT).show();
        }

        // Navigate to the RegisterActivity
        Intent intent = new Intent(getActivity(), ActivityRegister.class);
        startActivity(intent);
        getActivity().finish(); // Finish the current activity to prevent going back
    }

    private void deleteAccount() {
        ApiService apiService = RetrofitClient.getClient(FragmentAccount.readToken(getActivity())).create(ApiService.class);
        Call<Void> call = apiService.deleteAccount();

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    logOff();
                } else {
                    Toast.makeText(getActivity(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
