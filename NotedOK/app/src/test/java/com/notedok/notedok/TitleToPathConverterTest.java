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

    @Test
    public void getTitleFromUniquePath() throws Exception {
        assertEquals("Hello, World", TitleToPathConverter.getInstance().getTitle("/Hello, World~~3465786348.txt"));
    }

    @Test
    public void getTitleFromUniquePathWithDoubleSepatator() throws Exception {
        assertEquals("Hello~~World", TitleToPathConverter.getInstance().getTitle("/Hello~~World~~3465786348.txt"));
    }

    @Test
    public void getTitleDecodeChars() throws Exception {
        assertEquals("Hello, World /?<>\\:*|\"^", TitleToPathConverter.getInstance().getTitle("/Hello, World (sl)(qst)(lt)(gt)(bsl)(col)(star)(pipe)(dqt)(crt)~~3465786348.txt"));
    }

    @Test
    public void getTitleFromMinimalPath() throws Exception {
        assertEquals("", TitleToPathConverter.getInstance().getTitle("/.txt"));
    }

    @Test
    public void getTitleFromShortPath() throws Exception {
        assertEquals("1", TitleToPathConverter.getInstance().getTitle("/1.txt"));
    }
}
