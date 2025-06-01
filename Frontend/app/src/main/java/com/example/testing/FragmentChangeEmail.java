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

import java.io.FileOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentChangeEmail extends Fragment {

    private String oldUsername = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_email, container, false);

        EditText etOldEmail = view.findViewById(R.id.etOldEmail);
        EditText etNewEmail = view.findViewById(R.id.etNewEmail);
        Button btnConfirmEmail = view.findViewById(R.id.btnConfirmEmail);

        Bundle args = getArguments();
        if (args != null) {
            oldUsername = args.getString("oldUsername");
        }

        btnConfirmEmail.setOnClickListener(v -> {
            String newEmail = etNewEmail.getText().toString();
            changeEmail(newEmail);
        });

        return view;
    }

    private void changeEmail(String newEmail) {
        ApiService apiService = RetrofitClient.getClient(FragmentAccount.getToken()).create(ApiService.class);
        Call<ChangeResponse> call = apiService.changeEmail(newEmail);

        call.enqueue(new Callback<ChangeResponse>() {
            @Override
            public void onResponse(Call<ChangeResponse> call, Response<ChangeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChangeResponse changeResponse = response.body();
                    Toast.makeText(getActivity(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                    ActivityLogin.updateUserData(oldUsername, changeResponse.getToken(), changeResponse.getValue(), getActivity()); // Replace with actual username or fetch it
                    getParentFragmentManager().popBackStack();
                } else {
                    // Log the response to get more details
                    Log.e("ChangeUsername", "Response Code: " + response.code() + " Message: " + response.message());
                    try {
                        Log.e("ChangeUsername", "Response Error Body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getActivity(), "Failed to update email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChangeResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

