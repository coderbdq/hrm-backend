package com.hrm.enterprise_platform.system.util;

public class CosineSimilarity {

    public static double similarity(double[] a, double[] b) {
        if (a.length != b.length) return 0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += Math.pow(a[i], 2);
            normB += Math.pow(b[i], 2);
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
