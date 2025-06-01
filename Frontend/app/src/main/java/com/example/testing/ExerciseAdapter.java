package com.example.testing;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<ExerciseDto> exercises;
    private FragmentManager fragmentManager;
    private OnExerciseChangedListener listener;
    private ApiService apiService;
    private boolean isClickable;

    public ExerciseAdapter(List<ExerciseDto> exercises, FragmentManager fragmentManager, OnExerciseChangedListener listener, Activity activity, boolean isClickable) {
        this.exercises = exercises;
        this.fragmentManager = fragmentManager;
        this.listener = listener;
        this.apiService = RetrofitClient.getClient(FragmentAccount.readToken((FragmentActivity) activity)).create(ApiService.class);
        this.isClickable = isClickable;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseDto exercise = exercises.get(position);
        holder.exerciseNameTextView.setText(exercise.getExerciseName());
        holder.exerciseBPMTextView.setText(String.format("BPM: %d", exercise.getExerciseBpm()));

        if (isClickable) {
            holder.itemView.setOnClickListener(v -> {
                fetchExerciseData(v.getContext(), exercise.getId(), exercise.getExerciseName());
            });
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseNameTextView;
        TextView exerciseBPMTextView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameTextView = itemView.findViewById(R.id.exerciseNameTextView);
            exerciseBPMTextView = itemView.findViewById(R.id.exerciseBPMTextView);
        }
    }

    private void fetchExerciseData(Context context, Long exerciseId, String exerciseName) {
        apiService.getExercise(exerciseId).enqueue(new Callback<ExerciseDto>() {
            @Override
            public void onResponse(Call<ExerciseDto> call, Response<ExerciseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExerciseDto exercise = response.body();
                    List<ViewGrid.Cell> selectedCells = readFromMidiData(context, exercise.getData());
                    DialogFragmentExercise dialogFragment = DialogFragmentExercise.newInstance(exerciseName, exercise.getExerciseBpm(), selectedCells, exercise.getId(), listener);
                    dialogFragment.show(fragmentManager, "ExerciseDialogFragment");
                } else {
                    Toast.makeText(context, "Failed to fetch exercise data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ExerciseDto> call, Throwable t) {
                Toast.makeText(context, "Error fetching exercise data", Toast.LENGTH_LONG).show();
            }
        });
    }
    public static List<ViewGrid.Cell> readFromMidiData(Context context, String encodedMidiData) {
        List<ViewGrid.Cell> selectedCells = new ArrayList<>();

        try {
            byte[] midiData = Base64.decode(encodedMidiData, Base64.DEFAULT);

            // Use ByteArrayInputStream to read the MIDI data
            ByteArrayInputStream inputStream = new ByteArrayInputStream(midiData);

            // Read the MIDI file
            MidiFile midi = new MidiFile(inputStream);

            // Get the tracks
            List<MidiTrack> tracks = midi.getTracks();

            if (tracks.isEmpty()) {
                Toast.makeText(context, "Invalid MIDI file format", Toast.LENGTH_LONG).show();
                return null;
            }

            // Iterate through all tracks
            for (MidiTrack track : tracks) {
                long startTick = 0;

                // Iterate through the events in the track
                for (MidiEvent event : track.getEvents()) {
                    if (event instanceof NoteOn) {
                        NoteOn noteOn = (NoteOn) event;
                        if (noteOn.getVelocity() != 0) {
                            startTick = noteOn.getTick();
                        } else {
                            long endTick = noteOn.getTick();
                            int pitch = noteOn.getNoteValue();
                            int row = pitch - 24;

                            while (startTick < endTick) {
                                int col = (int) (startTick / 120);
                                selectedCells.add(new ViewGrid.Cell(row, col));
                                startTick += 120;
                            }
                        }
                    } else if (event instanceof NoteOff) {
                        NoteOff noteOff = (NoteOff) event;
                        long endTick = noteOff.getTick();
                        int pitch = noteOff.getNoteValue();
                        int row = pitch - 24;

                        while (startTick < endTick) {
                            int col = (int) (startTick / 120);
                            selectedCells.add(new ViewGrid.Cell(row, col));
                            startTick += 120;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error loading exercise", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return selectedCells;
    }


}
