package com.example.testing;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

public class FragmentExerciseEditor extends Fragment {
    ViewGrid gridView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_editor, container, false);

        gridView = view.findViewById(R.id.GridView);

        // Retrieve the selected cells from the arguments
        Bundle args = getArguments();
        if (args != null) {
            List<ViewGrid.Cell> selectedCells = (List<ViewGrid.Cell>) args.getSerializable("selectedCells");
            if (selectedCells != null) {
                gridView.setSelectedCells(selectedCells);
                gridView.ensureExtraColumns();
            }
        }

        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        Button doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> {
            // Show the DialogFragment
            DialogFragmentDoneEditing dialogFragment = new DialogFragmentDoneEditing(gridView.getSelectedCells());
            dialogFragment.show(getChildFragmentManager(), "dialog_tag");
        });

        return view;
    }
}
