package com.example.gymnotes;

/** Shared validation for inline input; incomplete values never overwrite a valid draft. */
final class SetInput {
    static Double weight(String text) {
        try {
            double value = Double.parseDouble(text.trim().replace(',', '.'));
            return Double.isFinite(value) && value >= 0 && value <= 10000 ? value : null;
        } catch (NumberFormatException e) { return null; }
    }
    static Integer reps(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value >= 1 && value <= 10000 ? value : null;
        } catch (NumberFormatException e) { return null; }
    }
}
