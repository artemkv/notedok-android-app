package com.notedok.notedok;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TitleToPathConverterTest {
    @Test
    public void getTitleFromPath() throws Exception {
        assertEquals("Hello, World", TitleToPathConverter.getInstance().getTitle("/Hello, World.txt"));
    }

    @Test
    public void getTitleFromSubPath() throws Exception {
        assertEquals("Hello, World", TitleToPathConverter.getInstance().getTitle("aaa/bbb/Hello, World.txt"));
    }
}
