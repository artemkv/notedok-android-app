package com.notedok.notedok;

import java.util.Date;

import okhttp3.internal.Util;

public final class TitleToPathConverter {
    // Single instance
    private static TitleToPathConverter Instance = new TitleToPathConverter();

    private static final String TITLE_POSTFIX_SEPARATOR = "~~";

    // Prevents instantiation
    private TitleToPathConverter() {
    }

    /**
     * Returns the instance of the converter
     * @return The instance of the converter
     */
    public static TitleToPathConverter getInstance() {
        return Instance;
    }

    /**
     * Gets the title from the file path
     * @param path The file path
     * @return The title
     */
    public String getTitle(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        String fileNameWithoutExtension = filename.substring(0, filename.length() - 4);

        String title = fileNameWithoutExtension;

        int separatorIndex = title.lastIndexOf(TITLE_POSTFIX_SEPARATOR);
        if (separatorIndex >= 0) {
            title = title.substring(0, separatorIndex);
        }

        title = decodePathFileSystemFriendly(title);

        return title;
    }

    /**
     * Generates the file path from the note title
     * @param note The note
     * @param ensureUnique True, if the file path needs the timestamp that guarantees the uniqueness of the generated path.
     * @return The file path.
     */
    public String generatePath(Note note, boolean ensureUnique) {
        String postfix = "";
        if (ensureUnique || note.Title == null || note.Title.isEmpty()) {
            Date date = new Date();
            long n = date.getTime();
            postfix = TITLE_POSTFIX_SEPARATOR + n;
        }
        return "/" + encodePathFileSystemFriendly(note.Title) + postfix + ".txt";
    }

    private String encodePathFileSystemFriendly(String path) {
        path = path.replace("/", "(sl)");
        path = path.replace("?", "(qst)");
        path = path.replace("<", "(lt)");
        path = path.replace(">", "(gt)");
        path = path.replace("\\", "(bsl)");
        path = path.replace(":", "(col)");
        path = path.replace("*", "(star)");
        path = path.replace("|", "(pipe)");
        path = path.replace("\"", "(dqt)");
        path = path.replace("^", "(crt)");

        if (path.startsWith(".")) {
            path = "_" + path;
        }

        return path;
    }

    private String decodePathFileSystemFriendly(String path) {
        path = path.replace("(sl)", "/");
        path = path.replace("(qst)", "?");
        path = path.replace("(lt)", "<");
        path = path.replace("(gt)", ">");
        path = path.replace("(bsl)", "\\");
        path = path.replace("(col)", ":");
        path = path.replace("(star)", "*");
        path = path.replace("(pipe)", "|");
        path = path.replace("(dqt)", "\"");
        path = path.replace("(crt)", "^");
        return path;
    }
}
