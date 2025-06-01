package com.example.testing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.meta.Tempo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends Fragment implements OnExerciseChangedListener, OnSharingChangedListener {

    private ExerciseAdapter adapter;
    private List<ExerciseDto> exercises;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button createExerciseButton = view.findViewById(R.id.createExerciseButton);
        Button settingsButton = view.findViewById(R.id.settingsButton);
        Button importButton = view.findViewById(R.id.importExerciseButton);
        Button notificationsButton = view.findViewById(R.id.notificationsButton);
        RecyclerView exercisesRecyclerView = view.findViewById(R.id.exercisesRecyclerView);

        createExerciseButton.setOnClickListener(v -> {
            Fragment exerciseFragment = new FragmentExerciseEditor();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, exerciseFragment)
                    .addToBackStack(null)
                    .commit();
        });

        settingsButton.setOnClickListener(v -> {
            Fragment settingsFragment = new FragmentAccount();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, settingsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        notificationsButton.setOnClickListener(v -> {
            apiService.findSharings().enqueue(new Callback<List<SharingDto>>() {
                @Override
                public void onResponse(Call<List<SharingDto>> call, Response<List<SharingDto>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DialogFragmentSharing dialog = DialogFragmentSharing.newInstance(response.body(), FragmentHome.this);
                        dialog.show(getParentFragmentManager(), "SharingDialog");
                    } else {
                        Toast.makeText(getContext(), "Failed to load sharings", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<SharingDto>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error loading sharings", Toast.LENGTH_SHORT).show();
                }
            });
        });

        importButton.setOnClickListener(v -> importMidiFile());

        // Initialize the list and adapter
        exercises = new ArrayList<>();
        adapter = new ExerciseAdapter(exercises, getParentFragmentManager(), this, getActivity(), true);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        exercisesRecyclerView.setAdapter(adapter);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient(FragmentAccount.readToken(getActivity())).create(ApiService.class);

        // Fetch exercises from the server
        fetchExercises();

        // Initialize the file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                importExerciseFromUri(uri);
                            }
                        }
                    }
                });

        return view;
    }

    private void importMidiFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/midi");
        filePickerLauncher.launch(intent);
    }
    private void importExerciseFromUri(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            int bpm = 0;
            if (inputStream == null) {
                Toast.makeText(getContext(), "Failed to open file", Toast.LENGTH_LONG).show();
                return;
            }

            String fileName = getFileName(uri);
            MidiFile midiFile = new MidiFile(inputStream);
            File file = File.createTempFile("temp_midi", "mid", getContext().getCacheDir());
            midiFile.writeToFile(file);
            byte[] midiData = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(midiData);
            }
            catch  (IOException e) {
                throw new RuntimeException(e);
            }


            // Get the tracks
            List<MidiTrack> tracks = midiFile.getTracks();

            if (tracks.isEmpty()) {
                Toast.makeText(getContext(), "Invalid MIDI file format", Toast.LENGTH_LONG).show();
            }

            // The first track is assumed to be the tempo track
            MidiTrack tempoTrack = tracks.get(0);

            // Iterate through the events in the tempo track to find the tempo
            for (MidiEvent event : tempoTrack.getEvents()) {
                if (event instanceof Tempo) {
                    Tempo tempo = (Tempo) event;
                    bpm = (int) tempo.getBpm();
                    break;
                }
            }

            // Create a request body for the MIDI file
            RequestBody midiFileRequestBody = RequestBody.create(MediaType.parse("audio/midi"), file);
            MultipartBody.Part midiFilePart = MultipartBody.Part.createFormData("file", fileName, midiFileRequestBody);

            // Create a request body for the exercise name and BPM
            RequestBody exerciseNameRequestBody = RequestBody.create(MediaType.parse("text/plain"), fileName.replace(".mid", ""));

            apiService.createExercise(exerciseNameRequestBody,midiFilePart, bpm).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    file.delete();
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Exercise imported successfully", Toast.LENGTH_LONG).show();
                        fetchExercises(); // Refresh the list of exercises
                    } else {
                        Toast.makeText(getContext(), "Failed to import exercise", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    file.delete();
                    Toast.makeText(getContext(), "Error importing exercise", Toast.LENGTH_LONG).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error importing file", Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void fetchExercises() {
        apiService.findExercises().enqueue(new Callback<List<ExerciseDto>>() {
            @Override
            public void onResponse(Call<List<ExerciseDto>> call, Response<List<ExerciseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    exercises.clear();
                    exercises.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to fetch exercises", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ExerciseDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching exercises", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onExerciseChanged() {
        fetchExercises();
    }

    @Override
    public void onSharingChanged() {
        fetchExercises();
    }
}
