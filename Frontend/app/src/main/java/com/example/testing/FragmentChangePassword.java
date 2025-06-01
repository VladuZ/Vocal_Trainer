package com.example.testing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentChangePassword extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        EditText etOldPassword = view.findViewById(R.id.etOldPassword);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        Button btnConfirmPassword = view.findViewById(R.id.btnConfirmPassword);

        btnConfirmPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString();
            changePassword(newPassword);
        });

        return view;
    }

    private void changePassword(String newPassword) {
        ApiService apiService = RetrofitClient.getClient(FragmentAccount.getToken()).create(ApiService.class);
        Call<ChangeResponse> call = apiService.changePassword(newPassword);

        call.enqueue(new Callback<ChangeResponse>() {
            @Override
            public void onResponse(Call<ChangeResponse> call, Response<ChangeResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChangeResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

