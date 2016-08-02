package com.notedok.notedok;

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

    public String getTitle(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        String fileNameWithoutExtension = filename.substring(0, filename.length() - 4);

        String title = fileNameWithoutExtension;

        int separatorIndex = title.lastIndexOf(TITLE_POSTFIX_SEPARATOR);
        if (separatorIndex >= 0) {
            title = title.substring(0, separatorIndex);
        }

        // TODO:
        // title = Util.decodePathFileSystemFriendly(title);

        return title;
    }
    public String generatePath(String title) {
        // TODO: implement
        return title;
    }
}
