package com.example.testing;

public class FourierTransform {

    public static void fft(double[] real, double[] imag) {
        int n = real.length;
        if (n == 0 || (n & (n - 1)) != 0) throw new IllegalArgumentException("Length must be power of 2");

        int logN = Integer.numberOfTrailingZeros(n);

        // Bit-reversal permutation
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - logN);
            if (j > i) {
                double tempReal = real[i];
                real[i] = real[j];
                real[j] = tempReal;
                double tempImag = imag[i];
                imag[i] = imag[j];
                imag[j] = tempImag;
            }
        }

        // Cooley-Tukey
        for (int len = 2; len <= n; len <<= 1) {
            double angle = -2 * Math.PI / len;
            double wlenReal = Math.cos(angle);
            double wlenImag = Math.sin(angle);

            for (int i = 0; i < n; i += len) {
                double wReal = 1, wImag = 0;
                for (int j = 0; j < len / 2; j++) {
                    int u = i + j;
                    int v = i + j + len / 2;

                    double realU = real[u], imagU = imag[u];
                    double realV = real[v] * wReal - imag[v] * wImag;
                    double imagV = real[v] * wImag + imag[v] * wReal;

                    real[u] = realU + realV;
                    imag[u] = imagU + imagV;
                    real[v] = realU - realV;
                    imag[v] = imagU - imagV;

                    double nextWReal = wReal * wlenReal - wImag * wlenImag;
                    wImag = wReal * wlenImag + wImag * wlenReal;
                    wReal = nextWReal;
                }
            }
        }
    }
}
