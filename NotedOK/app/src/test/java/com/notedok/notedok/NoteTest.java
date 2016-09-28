package com.notedok.notedok;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoteTest {
    @Test
    public void create() throws Exception {
        Note note = new Note();
        note.setPath("/Hello, World.txt");
        note.setTitle("Hello, World");
        note.setText("This is my first note");
        note.setIsLoaded(true);

        assertEquals("Hello, World", note.getTitle());
        assertEquals("This is my first note", note.getText());
        assertEquals("/Hello, World.txt", note.getPath());
        assertEquals(true, note.getIsLoaded());
    }
}
