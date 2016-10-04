package com.notedok.notedok;

/**
 * Holds the list of files that were found by the last search.
 */
public class FileList {
    // File paths
    private String[] _files = new String[0];

    /**
     * Initializes a new instance of FileList class.
     * @param files The file list.
     */
    public FileList(String[] files) {
        if (files == null)
            throw new IllegalArgumentException("files");

        _files = files;
    }

    /**
     * Returns the length of the file list.
     * @return The length of the file list.
     */
    public int getLength() {
        return _files.length;
    }

    /**
     * Returns the file path at the specified position.
     * @param position The position of the file path
     * @return The file path at the specified position.
     */
    public String getPath(int position) {
        if (position < 0 || position >= _files.length) {
            throw new IllegalArgumentException("index value is outside of list boundaries");
        }
        return _files[position];
    }
}
