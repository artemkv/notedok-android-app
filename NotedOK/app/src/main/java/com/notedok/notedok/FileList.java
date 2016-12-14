package com.notedok.notedok;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Holds the list of files that were found by the last search.
 * Is not thread safe. Every activity should get a private copy of this list.
 */
public class FileList {
    // File paths
    private ArrayList<String> _files = new ArrayList<String>();

    /**
     * Initializes a new instance of FileList class.
     * @param files The file list.
     */
    public FileList(ArrayList<String> files) {
        if (files == null)
            throw new IllegalArgumentException("files");

        _files = files;
    }

    /**
     * Returns the length of the file list.
     * @return The length of the file list.
     */
    public int getLength() {
        return _files.size();
    }

    /**
     * Returns the file path at the specified position.
     * @param position The position of the file path
     * @return The file path at the specified position.
     */
    public String getPath(int position) {
        if (position < 0 || position >= _files.size()) {
            throw new IllegalArgumentException("index value is outside of list boundaries");
        }
        return _files.get(position);
    }

    /**
     * Returns the file list that can be serialized.
     * @return The list of files.
     */
    public ArrayList<String> getAsArrayList() {
        return _files;
    }

    /**
     * Removes the item at the specified position from the list.
     * @param position The position of the file path
     */
    public void remove(int position) {
        if (position < 0 || position >= _files.size()) {
            throw new IllegalArgumentException("index value is outside of list boundaries");
        }
        _files.remove(position);
    }
}
