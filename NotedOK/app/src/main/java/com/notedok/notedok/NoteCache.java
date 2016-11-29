package com.notedok.notedok;

import java.util.HashMap;

/**
 * Temporary storage for notes. Thread-safe.
 * The notes can be used by the same activity or multiple activities.
 * By re-using the same instances we reduce the memory pressure and avoid re-loading content again and again.
 * We normally clean up the cache every time we refresh the note list.
 * So there is no need to implement the logic to keep only the last 50 notes or something similar.
 */
public final class NoteCache {
    // Single instance
    private static NoteCache Instance = new NoteCache();

    // Here the notes are actually cached
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

    /**
     * Returns the note for the given path. Returned note can be loaded or not.
     * @param path The note path (the file path of the .txt file where the note is stored).
     * @return The instance of the note. This note can be loaded or not.
     */
    public Note getNote(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path");
        }

        synchronized (this) {
            if (_notes.containsKey(path)) {
                return _notes.get(path);
            }
        }

        Note note = new Note();
        note.setPath(path);
        note.setTitle(TitleToPathConverter.getInstance().getTitle(path));
        note.setText("");
        note.setIsLoaded(false);

        synchronized (this) {
            // In the extremely unlikely scenario replaces the note, but it's not critical.
            // The worst case 2 activities will get 2 different instances on note and will have to load them both.
            _notes.put(path, note);
        }

        return note;
    }

    /**
     * Clears the cache.
     * Normally is called when the list of notes is refreshed.
     */
    public void clear() {
        synchronized (this) {
            _notes = new HashMap<String, Note>();
        }
    }

    /**
     * Removes the note from cache to force reloading it
     * @param path The note path
     */
    public void removeFromCache(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path");
        }

        synchronized (this) {
            if (_notes.containsKey(path)) {
                _notes.remove(path);
            }
        }
    }
}
