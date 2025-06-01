package com.example.testing;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterExerciseBPM implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (!isValidCharacter(c)) {
                return ""; // Reject the character
            }
        }

        return null; // Accept the character
    }

    private boolean isValidCharacter(char c) {
        // Allow numbers
        return Character.isDigit(c);
    }
}
