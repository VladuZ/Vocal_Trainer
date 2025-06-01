package com.example.testing;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentChangeUsername extends Fragment {

    private String oldEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_username, container, false);

        EditText etOldUsername = view.findViewById(R.id.etOldUsername);
        EditText etNewUsername = view.findViewById(R.id.etNewUsername);
        Button btnConfirmUsername = view.findViewById(R.id.btnConfirmUsername);

        Bundle args = getArguments();
        if (args != null) {
            oldEmail = args.getString("oldEmail");
        }

        btnConfirmUsername.setOnClickListener(v -> {
            String newUsername = etNewUsername.getText().toString();
            changeUsername(newUsername);
        });

        return view;
    }

    private void changeUsername(String newUsername) {
        ApiService apiService = RetrofitClient.getClient(FragmentAccount.getToken()).create(ApiService.class);
        Call<ChangeResponse> call = apiService.changeUsername(newUsername);
        call.enqueue(new Callback<ChangeResponse>() {
            @Override
            public void onResponse(Call<ChangeResponse> call, Response<ChangeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChangeResponse changeResponse = response.body();
                    Toast.makeText(getActivity(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                    ActivityLogin.updateUserData(changeResponse.getValue(), changeResponse.getToken(), oldEmail, getActivity()); // Replace with actual email or fetch it
                    getParentFragmentManager().popBackStack();
                } else {
                    // Log the response to get more details
                    Log.e("ChangeUsername", "Response Code: " + response.code() + " Message: " + response.message());
                    try {
                        Log.e("ChangeUsername", "Response Error Body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(), "Failed to update username", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChangeResponse> call, Throwable t) {
                Log.e("ChangeUsername", "Error: " + t.getMessage());
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

