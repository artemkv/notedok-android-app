package com.notedok.notedok;

import java.util.HashMap;

public final class NoteCache {
    // Single instance
    private static NoteCache Instance = new NoteCache();

    // TODO: Should notes stay here forever or should we keep only last 50 or something?
    private HashMap<String, Note> _notes = new HashMap<String, Note>();

    // Prevents instantiation
    private NoteCache() {
    }

    /**
     * Returns the instance of the note cache
     * @return The instance of the note cache
     */
    public static NoteCache getInstance() {
        return Instance;
    }

    public Note getNote(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path");
        }

        // TODO: Needs to be synchronized?
        if (_notes.containsKey(path)) {
            return _notes.get(path);
        }

        Note note = new Note();
        note.setPath(path);
        note.setTitle(TitleToPathConverter.getInstance().getTitle(path));
        note.setText("Loading..."); // TODO: Replace with actual spinner?
        note.setIsLoaded(false);

        // TODO: Needs to be synchronized?
        _notes.put(path, note);

        return note;
    }

    public void clear() {
        _notes = new HashMap<String, Note>();
    }
}
