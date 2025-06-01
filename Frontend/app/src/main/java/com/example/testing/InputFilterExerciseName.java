package com.example.testing;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterExerciseName implements InputFilter {

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
        // Allow Latin letters and numbers
        return Character.isLetterOrDigit(c);
    }
}
