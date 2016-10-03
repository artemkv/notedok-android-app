package com.notedok.notedok;

/**
 * Holds the list of files that were found by the last search.
 */
public final class CurrentFileList {
    // Single instance
    private static CurrentFileList Instance = new CurrentFileList();

    // File paths
    private String[] _files = new String[0];

    // The list version.
    private int _version = 0;

    // Prevents instantiation
    private CurrentFileList() {
    }

    /**
     * Returns the instance of the file list
     * @return The instance of the file list
     */
    public static CurrentFileList getInstance() {
        return Instance;
    }

    /**
     * Clears the current file list.
     * Normally not needed, since once cleared and before reloaded, any async action on notes might fail.
     */
    public void clear() {
        raiseVersion();
        _files = new String[0];
    }

    /**
     * Reloads the file list from the specified array of file paths.
     * @param files The array of file paths. If null, the list is reset to empty.
     */
    public void reload(String[] files) {
        raiseVersion();
        if (files == null) {
            _files = new String[0];
        } else {
            _files = files;
        }
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

    /**
     * Returns the list version.
     * @return The list version.
     */
    public int getVersion() {
        return _version;
    }

    private void raiseVersion() {
        _version++;
    }
}
