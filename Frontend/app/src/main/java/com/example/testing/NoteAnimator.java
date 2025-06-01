package com.example.testing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NoteAnimator extends View {

    private List<Note> notes = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable runnable;
    private AudioAnalyzer audioAnalyzer;
    private TextView noteTextView;
    private FragmentManager fragmentManager;
    public static final int REFRESH_RATE = 16;
    public static double NOTE_SPEED = 0;
    boolean atLeastOneNote = false;
    boolean exerciseActive = true;
    private static final int NUM_PITCHES = 60; // Updated to 60 pitches (5 octaves * 12 notes)
    private List<Key> pianoKeys = new ArrayList<>();
    private int currentPitchIndex = -1;
    private int nextPitchIndex = -1;
    private int sungPitch = -1;
    boolean isInit = false;
    private final String[] NOTE_NAMES = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    private static final int[] NOTE_COLORS = {
            Color.RED, Color.MAGENTA, Color.rgb(255,165,0), Color.rgb(255,105,180),
            Color.YELLOW, Color.GREEN, Color.rgb(0,128,128), Color.BLUE,
            Color.CYAN, Color.rgb(128,0,128), Color.rgb(255,20,147), Color.LTGRAY
    };
    public NoteAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    private void init() {
        audioAnalyzer = new AudioAnalyzer(getContext());
        runnable = new Runnable() {
            @Override
            public void run() {
                updateNotes();
                invalidate();
                handler.postDelayed(this, REFRESH_RATE);
            }
        };

        handler.post(runnable);
    }

    private boolean isBlackKey(int pitchIndex) {
        int[] blackNotes = {1, 3, 6, 8, 10};
        for (int i : blackNotes) if (i == pitchIndex % 12) return true;
        return false;
    }

    private void initPianoKeys() {
        pianoKeys.clear();
        int height = getHeight();
        int whiteKeyCount = 0;

        for (int i = 0; i < NUM_PITCHES; i++) {
            if (!isBlackKey(i)) whiteKeyCount++;
        }

        float whiteKeyHeight = (float) height / whiteKeyCount;
        float y = 0;

        for (int i = 0; i < NUM_PITCHES; i++) {
            if (isBlackKey(i)) {
                float blackHeight = whiteKeyHeight * 0.6f;
                float blackY = y - blackHeight / 2;
                pianoKeys.add(new Key(blackY, blackHeight, true));
            } else {
                pianoKeys.add(new Key(y, whiteKeyHeight, false));
                y += whiteKeyHeight;
            }
        }
    }

    public void addNote(int pitchIndex, int lengthPx, int offsetX) {
        if (pitchIndex < 0 || pitchIndex >= pianoKeys.size()) return;

        notes.add(new Note(1000 + offsetX, lengthPx, pitchIndex));

        if(!atLeastOneNote)
            atLeastOneNote = true;
    }

    private void updateNotes() {
        if(exerciseActive) {
            Iterator<Note> iterator = notes.iterator();
            currentPitchIndex = -1;
            nextPitchIndex = -1;

            while (iterator.hasNext()) {
                Note note = iterator.next();
                note.x -= NOTE_SPEED;
                if (note.x + note.width < 0) {
                    iterator.remove();
                } else if (currentPitchIndex == -1 && note.x <= getWidth() * 0.9) {
                    currentPitchIndex = note.pitchIndex;
                } else if (nextPitchIndex == -1 && note.x <= getWidth() * 0.9 && note.pitchIndex != currentPitchIndex) {
                    nextPitchIndex = note.pitchIndex;
                }
            }
            sungPitch = audioAnalyzer.getCurrentPitchIndex();
            updateNoteText(currentPitchIndex, nextPitchIndex, sungPitch);
            if (notes.isEmpty() && atLeastOneNote) {
                // Show the DialogFragment
                handler.postDelayed(() -> {
                    if(!fragmentManager.isDestroyed()) {
                        DialogFragmentExerciseDone dialogFragment = new DialogFragmentExerciseDone();
                        dialogFragment.show(fragmentManager, "dialog_tag");
                    }
                }, 500);
                atLeastOneNote = false;
                exerciseActive = false;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pianoKeys.isEmpty()) initPianoKeys();

        if(!isInit) {
            init();
            isInit = true;
        }

        for (Note note : notes) {
            Key key = pianoKeys.get(note.pitchIndex);
            int y = (int) key.y;
            int height = (int) key.height;

            int color = NOTE_COLORS[note.pitchIndex % 12];
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect((int)note.x, y, (int)note.x + note.width, y + height, paint);
        }

        // Візуалізація співаної ноти
        if (sungPitch != -1 && sungPitch < pianoKeys.size()) {
            Key key = pianoKeys.get(sungPitch);
            int y = (int) key.y;
            int height = (int) key.height;
            Paint sungPaint = new Paint();
            sungPaint.setColor(Color.argb(100, 0, 255, 0)); // прозоро-зелений
            sungPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, y, getWidth(), y + height, sungPaint);
        }
    }



    public int getCurrentPitchIndex() {
        return currentPitchIndex;
    }

    public void setSungPitch(int pitchIndex) {
        this.sungPitch = pitchIndex;
    }

    public void setBPM(int bpm, int length){
        NOTE_SPEED = (double)(REFRESH_RATE * bpm * length) / 15000f;
    }

    private void updateNoteText(int currentPitch, int nextPitch, int sungPitch) {
        String currentNote = getNoteName(currentPitch);
        String nextNote = getNoteName(nextPitch);
        String sungNote = getNoteName(sungPitch);

        String accuracy = (sungPitch != -1 && sungPitch == currentPitch) ? "Yes" :
                (sungPitch != -1 && currentPitch != -1) ? "No" : "—";

        String display = "Current: " + currentNote +
                "\nNext: " + nextNote +
                "\nYour: " + sungNote +
                "\nMatch: " + accuracy;

        try {
            noteTextView.setText(display);
        }
        catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

    private String getNoteName(int pitchIndex) {
        if (pitchIndex == -1) return "—";
        int octave = (pitchIndex / 12) + 1; // Updated to start from octave 1
        int note = pitchIndex % 12;
        return NOTE_NAMES[note] + octave;
    }

    public void setNoteTextView(TextView noteTextView){
        this.noteTextView = noteTextView;
    }
    public void setFragmentManager(FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
    }

    private static class Note {
        double x;
        int  width, pitchIndex;

        Note(int x, int width, int pitchIndex) {
            this.x = x;
            this.width = width;
            this.pitchIndex = pitchIndex;
        }
    }

    private static class Key {
        float y, height;
        boolean isBlack;

        Key(float y, float height, boolean isBlack) {
            this.y = y;
            this.height = height;
            this.isBlack = isBlack;
        }
    }

    public void reset() {
        notes.clear();
        currentPitchIndex = -1;
        nextPitchIndex = -1;
        sungPitch = -1;
        exerciseActive = true;
        atLeastOneNote = false;
    }

}