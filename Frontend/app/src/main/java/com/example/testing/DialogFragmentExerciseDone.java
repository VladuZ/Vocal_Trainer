package com.example.testing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

public class DialogFragmentExerciseDone extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_exercise_done, container, false);
        Button buttonBack = view.findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> {
            // Handle play button click
            FragmentHome homeFragment = new FragmentHome();

            // Replace the current fragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, homeFragment)
                    .addToBackStack(null)
                    .commit();

            dismiss();
        });

        return view;
    }
}
