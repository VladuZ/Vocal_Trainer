package com.example.testing;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class ViewEditorPiano extends ViewPiano {


    private final int numWhiteKeys = 60; // 5 octaves

    public ViewEditorPiano(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPiano(canvas);
        drawNotes(canvas);
    }

    @Override
    protected void drawPiano(Canvas canvas) {
        float keyHeight = (float) getHeight() / numWhiteKeys;
        float keyWidth = getWidth();

        int pitchIndex = 0;

        // Draw white keys with labels
        for (int i = 0; i < numWhiteKeys; i++) {
            float top = i * keyHeight;
            if (top > getHeight() || top + keyHeight < 0) continue; // Skip if out of view

            String noteName = NOTE_NAMES[pitchIndex % 12] + (pitchIndex / 12 + 1);
            canvas.drawRect(0, top, keyWidth, top + keyHeight, whitePaint);
            canvas.drawRect(0, top, keyWidth, top + keyHeight, borderPaint);
            canvas.drawText(noteName, 20, top + keyHeight * 0.6f, textPaint);
            pitchIndex++;
        }

        // Draw black keys with labels after white keys
        pitchIndex = 0;
        for (int i = 0; i < numWhiteKeys; i++) {
            float top = i * keyHeight;
            if (top > getHeight() || top + keyHeight < 0) continue; // Skip if out of view

            if (isBlackKey(pitchIndex)) {
                String noteName = NOTE_NAMES[pitchIndex % 12] + (pitchIndex / 12 + 1);
                canvas.drawRect(0, top, keyWidth, top + keyHeight, blackPaint);
                canvas.drawRect(0, top, keyWidth, top + keyHeight, borderPaint);
                canvas.drawText(noteName, 20, top + keyHeight * 0.6f, textOnBlackPaint);
            }
            pitchIndex++;
        }
    }

    private void drawNotes(Canvas canvas) {
        if (notesList != null) {
            float keyHeight = (float) getHeight() / numWhiteKeys;
            for (Note note : notesList) {
                int position = note.getPosition();
                int length = note.getLength();
                float top = position * keyHeight;
                if (top > getHeight() || top + length * keyHeight < 0) continue; // Skip if out of view
                canvas.drawRect(0, top, getWidth(), top + length * keyHeight, notePaint);
            }
        }
    }
}
