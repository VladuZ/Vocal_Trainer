package com.example.testing;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.leff.midi.*;
import com.leff.midi.event.meta.Tempo;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogFragmentDoneEditing extends DialogFragment {

    private EditText exerciseNameEditText;
    private EditText exerciseBPMEditText;
    private Button buttonOk;
    private List<ViewGrid.Cell> selectedCells;
    private ApiService apiService;

    public DialogFragmentDoneEditing(List<ViewGrid.Cell> selectedCells) {
        super();
        this.selectedCells = selectedCells;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_done_editing, container, false);

        exerciseNameEditText = view.findViewById(R.id.nameText);
        exerciseBPMEditText = view.findViewById(R.id.bpmText);
        buttonOk = view.findViewById(R.id.buttonOk);

        exerciseNameEditText.setFilters(new InputFilter[]{new InputFilterExerciseName()});
        exerciseBPMEditText.setFilters(new InputFilter[]{new InputFilterExerciseBPM()});

        apiService = RetrofitClient.getClient(FragmentAccount.readToken(getActivity())).create(ApiService.class);

        buttonOk.setOnClickListener(v -> {
            Context context = getActivity();
            if (context == null) {
                return;
            }

            String exerciseName = exerciseNameEditText.getText().toString();
            String bpmText = exerciseBPMEditText.getText().toString();

            if (exerciseName.isEmpty() || bpmText.isEmpty()) {
                Toast.makeText(context, "Exercise name and BPM cannot be empty", Toast.LENGTH_LONG).show();
                return;
            }

            int bpm;
            try {
                bpm = Integer.parseInt(bpmText);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid BPM value", Toast.LENGTH_LONG).show();
                return;
            }

            if (bpm < 60 || bpm > 300) {
                Toast.makeText(context, "BPM should be in range from 60 to 300", Toast.LENGTH_LONG).show();
                return;
            }

            // Save to file
            saveToDatabase(context, exerciseName, bpm);
        });
        return view;
    }

    private void saveToDatabase(Context context, String exerciseName, int bpm) {
        try {
            // Create a new MIDI file
            List<MidiTrack> tracks = new ArrayList<>();
            MidiTrack tempoTrack = new MidiTrack();
            MidiTrack noteTrack = new MidiTrack();

            // Set tempo
            Tempo tempo = new Tempo();
            tempo.setBpm(bpm);
            tempoTrack.insertEvent(tempo);

            // Add tempo track to the list of tracks
            tracks.add(tempoTrack);

            // Add notes to the note track
            long duration = 120;
            long tick = 0;
            for (int i = 0; i < selectedCells.size() - 1; i++) {
                if (selectedCells.get(i + 1).row == selectedCells.get(i).row && selectedCells.get(i + 1).col == selectedCells.get(i).col + 1) {
                    duration += 120; // Duration of the note in ticks
                } else {
                    int pitch = selectedCells.get(i).row + 24;
                    // Insert Note On and Note Off events
                    noteTrack.insertNote(0, pitch, 100, tick, duration);
                    duration = 120;
                    tick = selectedCells.get(i + 1).col * 120;
                }
            }
            noteTrack.insertNote(0, selectedCells.get(selectedCells.size() - 1).row + 24, 100, tick, duration);

            // Add note track to the list of tracks
            tracks.add(noteTrack);

            // Create MIDI file
            MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

            // Convert MIDI file to byte array
            File tempfile = File.createTempFile("temp_midi", "mid", context.getCacheDir());
            midi.writeToFile(tempfile);
            byte[] midiData = new byte[(int) tempfile.length()];
            try (FileInputStream fis = new FileInputStream(tempfile)) {
                fis.read(midiData);
            }
            catch  (IOException e) {
                throw new RuntimeException(e);
            }

            // Create a request body for the MIDI file
            RequestBody midiFileRequestBody = RequestBody.create(MediaType.parse("audio/midi"), midiData);
            MultipartBody.Part midiFilePart = MultipartBody.Part.createFormData("file", exerciseName + ".mid", midiFileRequestBody);
            RequestBody exerciseNameRequestBody = RequestBody.create(MediaType.parse("text/plain"), exerciseName);
            apiService.createExercise(exerciseNameRequestBody, midiFilePart, bpm).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Exercise saved successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Failed to save exercise", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Error saving exercise", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving exercise", Toast.LENGTH_LONG).show();
        }
    }
}
