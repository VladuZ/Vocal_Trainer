package com.example.testing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

public class ViewPiano extends View {

    protected Paint whitePaint;
    protected Paint blackPaint;
    protected Paint borderPaint;
    protected Paint textPaint;
    protected Paint textOnBlackPaint;
    protected Paint dividerPaint;
    protected Paint notePaint;
    protected List<Note> notesList;

    private final int numWhiteKeys = 35; // 5 octaves
    private final int[] blackKeyPattern = {1, 1, 0, 1, 1, 1, 0};
    protected final String[] NOTE_NAMES = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    public ViewPiano(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStyle(Paint.Style.FILL);

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);

        textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(22);
        textPaint.setAntiAlias(true);

        textOnBlackPaint = new Paint();
        textOnBlackPaint.setColor(Color.WHITE);
        textOnBlackPaint.setTextSize(22);
        textOnBlackPaint.setAntiAlias(true);

        dividerPaint = new Paint();
        dividerPaint.setColor(Color.LTGRAY);
        dividerPaint.setStrokeWidth(4);

        notePaint = new Paint();
        notePaint.setColor(Color.RED);
        notePaint.setStyle(Paint.Style.FILL);
    }

    protected void setNotes(List<Note> notesList) {
        this.notesList = notesList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPiano(canvas);
        drawNotes(canvas);
    }

    protected void drawPiano(Canvas canvas) {
        float keyHeight = (float) getHeight() / numWhiteKeys;
        float keyWidth = getWidth();

        int pitchIndex = 0;
        int whiteKeyCount = 0;

        // Draw white keys with labels
        for (int i = 0; i < numWhiteKeys; i++) {
            float top = i * keyHeight;

            while (isBlackKey(pitchIndex)) {
                pitchIndex++;
            }

            String noteName = NOTE_NAMES[pitchIndex % 12] + ((pitchIndex / 12) + 1);
            canvas.drawRect(0, top, keyWidth, top + keyHeight, whitePaint);
            canvas.drawRect(0, top, keyWidth, top + keyHeight, borderPaint);
            canvas.drawText(noteName, 20, top + keyHeight * 0.6f, textPaint);

            whiteKeyCount++;
            pitchIndex++;

            if (whiteKeyCount % 7 == 0 && whiteKeyCount < numWhiteKeys) {
                canvas.drawLine(0, top + keyHeight, keyWidth, top + keyHeight, dividerPaint);
            }
        }

        // Draw black keys with labels
        int octaveCount = 5;
        int whiteKeyIndex = 0;
        int blackNoteCounter = 0;

        for (int octave = 0; octave < octaveCount; octave++) {
            for (int i = 0; i < blackKeyPattern.length; i++) {
                if (blackKeyPattern[i] == 1) {
                    int blackKeyIndex = whiteKeyIndex + i;
                    if (blackKeyIndex >= numWhiteKeys - 1) break;

                    float top = (blackKeyIndex + 1) * keyHeight - keyHeight * 0.3f;
                    float blackKeyHeight = keyHeight * 0.6f;
                    float blackKeyWidth = keyWidth * 0.6f;
                    float left = (keyWidth - blackKeyWidth) / 1.1f;

                    canvas.drawRect(left, top, left + blackKeyWidth, top + blackKeyHeight, blackPaint);

                    // Label for black key
                    int currentPitch = getBlackPitchIndex(blackNoteCounter);
                    String noteName = NOTE_NAMES[currentPitch % 12] + ((currentPitch / 12) + 1);
                    canvas.drawText(noteName, left + 10, top + blackKeyHeight / 2 + 8, textOnBlackPaint);

                    blackNoteCounter++;
                }
            }
            whiteKeyIndex += 7;
        }
    }

    private void drawNotes(Canvas canvas) {
        if (notesList != null) {
            float keyHeight = (float) getHeight() / numWhiteKeys;
            for (Note note : notesList) {
                int position = note.getPosition();
                int length = note.getLength();
                float top = position * keyHeight;
                canvas.drawRect(0, top, getWidth(), top + length * keyHeight, notePaint);
            }
        }
    }

    protected boolean isBlackKey(int pitchIndex) {
        int note = pitchIndex % 12;
        return note == 1 || note == 3 || note == 6 || note == 8 || note == 10;
    }

    private int getBlackPitchIndex(int blackNoteCounter) {
        int count = 0;
        for (int i = 0; i < 60; i++) {
            if (isBlackKey(i)) {
                if (count == blackNoteCounter) {
                    return i;
                }
                count++;
            }
        }
        return 1; // fallback (C#)
    }
}
