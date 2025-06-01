package com.example.testing;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.Manifest;

import androidx.core.app.ActivityCompat;

public class AudioAnalyzer {
    private static final int SAMPLE_RATE = 8000;
    private static final int BUFFER_SIZE = 2048;
    private static final int NUM_PITCHES = 60; // Updated to 60 pitches (5 octaves * 12 notes)

    private AudioRecord audioRecord;
    private Thread recordingThread;
    private volatile int currentPitchIndex = -1;

    public AudioAnalyzer(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE
            );
            startRecording();
        } else {
            // Обробка ситуації, коли дозвіл не надано
            Log.e("AudioAnalyzer", "Permission not granted!");
        }

    }

    public int getCurrentPitchIndex() {
        return currentPitchIndex;
    }

    private void startRecording() {
        audioRecord.startRecording();

        recordingThread = new Thread(() -> {
            short[] buffer = new short[BUFFER_SIZE];
            while (true) {
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (read > 0) {
                    double freq = analyzeFrequency(buffer, read);
                    currentPitchIndex = frequencyToPitchIndex(freq);
                }
            }
        });

        recordingThread.setDaemon(true);
        recordingThread.start();
    }

    private double analyzeFrequency(short[] audioBuffer, int readSize) {
        double[] real = new double[readSize];
        double[] imag = new double[readSize];

        for (int i = 0; i < readSize; i++) {
            real[i] = audioBuffer[i];
            imag[i] = 0;
        }

        FourierTransform.fft(real, imag);

        int maxIndex = 0;
        double maxMag = 0;

        for (int i = 0; i < readSize / 2; i++) {
            double magnitude = Math.sqrt(real[i]*real[i] + imag[i]*imag[i]);
            if (magnitude > maxMag) {
                maxMag = magnitude;
                maxIndex = i;
            }
        }

        return (double) maxIndex * SAMPLE_RATE / readSize;
    }

    private int frequencyToPitchIndex(double freq) {
        if (freq < 50 || freq > 2000) return -1;

        double baseFreq = 261.63; // C4
        int index = (int) Math.round(12 * Math.log(freq / baseFreq) / Math.log(2)) + 36; // Updated to start from octave 1
        return (index >= 0 && index < NUM_PITCHES) ? index : -1;
    }
}
