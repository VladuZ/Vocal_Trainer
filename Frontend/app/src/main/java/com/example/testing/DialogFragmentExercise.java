package com.example.testing;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.leff.midi.MidiFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogFragmentExercise extends DialogFragment {

    private static final String ARG_EXERCISE_NAME = "exerciseName";
    private static final String ARG_EXERCISE_BPM = "exerciseBpm";
    private static final String ARG_EXERCISE_CELLS = "exerciseCells";
    private static final String ARG_EXERCISE_ID = "exerciseId";

    private String exerciseName;
    private int exerciseBpm;
    private List<ViewGrid.Cell> exerciseCells;
    private Long exerciseId;
    private OnExerciseChangedListener listener;
    private MediaPlayer mediaPlayer;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> fileSaveLauncher;

    public static DialogFragmentExercise newInstance(String name, int bpm, List<ViewGrid.Cell> cells, Long id, OnExerciseChangedListener listener) {
        DialogFragmentExercise fragment = new DialogFragmentExercise();
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_NAME, name);
        args.putInt(ARG_EXERCISE_BPM, bpm);
        args.putSerializable(ARG_EXERCISE_CELLS, new ArrayList<>(cells));
        args.putLong(ARG_EXERCISE_ID, id);
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(OnExerciseChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exerciseName = getArguments().getString(ARG_EXERCISE_NAME);
            exerciseBpm = getArguments().getInt(ARG_EXERCISE_BPM);
            exerciseCells = (List<ViewGrid.Cell>) getArguments().getSerializable(ARG_EXERCISE_CELLS);
            exerciseId = getArguments().getLong(ARG_EXERCISE_ID);
        }
        apiService = RetrofitClient.getClient(FragmentAccount.readToken(getActivity())).create(ApiService.class);

        // Initialize the file save launcher
        fileSaveLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                manageSavedExercise(dataa -> {
                                    if (dataa != null) {
                                        exportFileToUri(dataa, uri);
                                    }
                                    else throw new RuntimeException("Idk2");
                                });
                            }
                        }
                    }
                });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_exercise, null);

        TextView exerciseNameTextView = view.findViewById(R.id.exerciseNameTextView);
        TextView exerciseBPMTextView = view.findViewById(R.id.exerciseBPMTextView);
        Button playButton = view.findViewById(R.id.playButton);
        Button editButton = view.findViewById(R.id.editButton);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button exportButton = view.findViewById(R.id.exportButton);
        Button playSavedButton = view.findViewById(R.id.playSavedButton);
        Button shareButton = view.findViewById(R.id.shareButton);

        exerciseNameTextView.setText(exerciseName);
        exerciseBPMTextView.setText(String.format("BPM: %d", exerciseBpm));

        playButton.setOnClickListener(v -> {
            // Handle play button click
            FragmentPlay playFragment = new FragmentPlay();

            // Pass the selected cells to the editor fragment
            Bundle args = new Bundle();
            args.putSerializable("selectedCells", new ArrayList<>(exerciseCells));
            args.putInt("exerciseBpm", exerciseBpm);
            playFragment.setArguments(args);

            // Replace the current fragment with the editor fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, playFragment)
                    .addToBackStack(null)
                    .commit();

            dismiss();
        });

        editButton.setOnClickListener(v -> {
            // Create a new instance of ExerciseEditorFragment
            FragmentExerciseEditor editorFragment = new FragmentExerciseEditor();

            // Pass the selected cells to the editor fragment
            Bundle args = new Bundle();
            args.putSerializable("selectedCells", new ArrayList<>(exerciseCells));
            editorFragment.setArguments(args);

            // Replace the current fragment with the editor fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, editorFragment)
                    .addToBackStack(null)
                    .commit();

            dismiss();
        });

        exportButton.setOnClickListener(v -> exportExerciseFile());

        deleteButton.setOnClickListener(v -> {
            deleteExercise();
            dismiss();
        });

        playSavedButton.setOnClickListener(v -> manageSavedExercise(data -> {
            if (data != null) {
                playMidiData(data);
            }
            else throw new RuntimeException("Idk");
        }));

        shareButton.setOnClickListener(v -> {
            DialogFragmentUsername fragment = new DialogFragmentUsername();
            Bundle args = new Bundle();
            args.putLong(ARG_EXERCISE_ID, exerciseId);
            fragment.setArguments(args);
            fragment.show(getActivity().getSupportFragmentManager(), "UsernameDialogFragment");
        });

        builder.setView(view);
        return builder.create();
    }

    private void manageSavedExercise(Consumer<String> onResult) {
        apiService.getExercise(exerciseId).enqueue(new Callback<ExerciseDto>() {
            @Override
            public void onResponse(Call<ExerciseDto> call, Response<ExerciseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExerciseDto exercise = response.body();
                    onResult.accept(exercise.getData());
                } else {
                    onResult.accept(null);
                    Toast.makeText(getContext(), "Failed to fetch exercise data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ExerciseDto> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching exercise data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playMidiData(String encodedMidiData) {
        Context context = getContext();
        byte[] midiData = Base64.decode(encodedMidiData, Base64.DEFAULT);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(midiData);
        try {
            MidiFile midifile = new MidiFile(inputStream);
            File tempfile = File.createTempFile("temp_midi", "mid", context.getCacheDir());
            midifile.writeToFile(tempfile);
            if (tempfile.exists()) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempfile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                tempfile.delete();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteExercise() {
        apiService.deleteExercise(exerciseId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (listener != null) {
                        listener.onExerciseChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to delete exercise", Toast.LENGTH_LONG).show();
                }
                dismiss();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error deleting exercise", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void exportExerciseFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/midi");
        intent.putExtra(Intent.EXTRA_TITLE, exerciseName + ".mid");
        fileSaveLauncher.launch(intent);
    }

    private void exportFileToUri(String encodedMidiData, Uri uri) {
        byte[] midiData = Base64.decode(encodedMidiData, Base64.DEFAULT);
        try (OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri)) {
            outputStream.write(midiData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
