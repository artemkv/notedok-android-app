package com.notedok.notedok;

/**
 * Represents a single note.
 */
public class Note {
    private String _path;
    private String _title;
    private String _text;
    private boolean _isLoaded;

    public void setPath(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        _path = value;
    }
    public String getPath() {
        return _path;
    }

    public void setTitle(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        _title = value;
    }
    public String getTitle() {
        return _title;
    }

    public void setText(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        _text = value;
    }
    public String getText() {
        return _text;
    }

    public void setIsLoaded(boolean value) {
        _isLoaded = value;
    }
    public boolean getIsLoaded() {
        return _isLoaded;
    }
}
