package com.example.testing;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FragmentPlay extends Fragment {
    private NoteAnimator noteAnimator;
    private TextView noteTextView;
    private Handler handler = new Handler();
    final int noteLength = 100;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        noteTextView = view.findViewById(R.id.noteText);
        noteAnimator = view.findViewById(R.id.noteAnimator);
        noteAnimator.setNoteTextView(noteTextView);
        noteAnimator.setFragmentManager(getChildFragmentManager());

        // Retrieve the selected cells from the arguments
        Bundle args = getArguments();
        if (args != null) {
            List<ViewGrid.Cell> selectedCells = (List<ViewGrid.Cell>) args.getSerializable("selectedCells");
            int exerciseBpm = args.getInt("exerciseBpm");
            Collections.sort(selectedCells, Comparator.comparingInt(v -> v.col));

            noteAnimator.reset();
            noteAnimator.setBPM(exerciseBpm, noteLength);

            int currentDelay = 0;

            for (int i = 0; i < selectedCells.size(); i++) {
                final int pitchIndex = selectedCells.get(i).row;
                int finalCurrentDelay = currentDelay;
                handler.postDelayed(() -> noteAnimator.addNote(pitchIndex, noteLength, finalCurrentDelay), 0);
                if (i + 1 < selectedCells.size()) {
                    currentDelay += noteLength * (selectedCells.get(i + 1).col - selectedCells.get(i).col);
                }
            }
        }

        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}