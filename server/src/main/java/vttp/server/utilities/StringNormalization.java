package vttp.server.utilities;

public class StringNormalization {

    // For characters that are non-standard
    public static String encodingNormalization(String str) {
        return str.replaceAll("\\p{Pd}", "-") // All dash punctuation (–, —, etc.) to -
                .replaceAll("[‘’`´]", "'") // Single quotes/apostrophes to '
                .replaceAll("[“”„]", "\"") // Double quotes to "
                .replaceAll("…", "...") // Ellipsis to ...
                .replaceAll("[\\u00A0\\u200B]", " ") // Non-breaking/zero-width space to regular space
                .replaceAll("•", "."); // Bullet to period
    }

}
