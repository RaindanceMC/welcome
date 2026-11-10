package de.raindancer.modules.welcome.model;

/**
 * A shot's caption as title and subtitle. A title is drawn four times as large, so at the usual GUI scale little
 * more than {@value #TITLE_FITS} letters fit across the screen; a longer caption without "Title | subtitle" goes on
 * the subtitle line instead of being cut off at both edges.
 */
public record Caption(String title, String subtitle) {

    static final int TITLE_FITS = 20;

    public static Caption of(String caption) {
        if (caption == null || caption.isBlank()) {
            return new Caption("", "");
        }
        String[] parts = caption.strip().split("\\s*\\|\\s*", 2);
        if (parts.length == 2) {
            return new Caption(parts[0], parts[1]);
        }
        return parts[0].length() > TITLE_FITS ? new Caption("", parts[0]) : new Caption(parts[0], "");
    }

    public boolean isEmpty() {
        return title.isEmpty() && subtitle.isEmpty();
    }
}
