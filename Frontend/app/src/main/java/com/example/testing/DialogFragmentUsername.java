package com.example.testing;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogFragmentUsername extends DialogFragment {
    private Long exerciseId;
    private ApiService apiService;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_username, null);

        if (getArguments() != null) {
            exerciseId = getArguments().getLong("exerciseId");
        }

        apiService = RetrofitClient.getClient(FragmentAccount.readToken(getActivity())).create(ApiService.class);

        builder.setView(view)
                .setPositiveButton("OK", (dialog, id) -> {
                    EditText editText = view.findViewById(R.id.editTextUsername);
                    String username = editText.getText().toString();
                    apiService.createSharing(exerciseId, username).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                //Toast.makeText(getContext(), "Shared succesfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to share", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Error sharing", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                   dismiss();
                });
        return builder.create();
    }
}
