package com.notedok.notedok;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WikiToHtmlFormatterTest {
    @Test
    public void trivialFormatting() throws Exception {
        String text = "Hello *world*!";
        String expectedText = "Hello <b>world</b>!\n";

        WikiToHtmlFormatter formatter = new WikiToHtmlFormatter();
        String formattedText = formatter.format(text);

        assertEquals("trivial formatting", expectedText, formattedText);
    }

    @Test
    public void basicFormatting() throws Exception {
        String text = "As a regular user, I want to be able to use basic formatting options to control how the text is rendered on the webpage.\n\n* *bold* is rendered as &lt;b&gt;bold&lt;/b&gt;\n* _italics_ is rendered as &lt;i&gt;italics&lt;/i&gt;\n* --deleted-- is rendered as &lt;del&gt;deleted&lt;/del&gt;\n* ++underline++ is rendered as &lt;u&gt;underline&lt;/u&gt;\n* ^superscript^ is rendered as &lt;sup&gt;superscript&lt;/sup&gt;\n* ~subscript~ is rendered as &lt;sub&gt;subscript&lt;/sub&gt;";
        String expectedText = "As a regular user, I want to be able to use basic formatting options to control how the text is rendered on the webpage.\n\n<ul><li><b>bold</b> is rendered as &lt;b&gt;bold&lt;/b&gt;</li>\n<li><i>italics</i> is rendered as &lt;i&gt;italics&lt;/i&gt;</li>\n<li><del>deleted</del> is rendered as &lt;del&gt;deleted&lt;/del&gt;</li>\n<li><u>underline</u> is rendered as &lt;u&gt;underline&lt;/u&gt;</li>\n<li><sup>superscript</sup> is rendered as &lt;sup&gt;superscript&lt;/sup&gt;</li>\n<li><sub>subscript</sub> is rendered as &lt;sub&gt;subscript&lt;/sub&gt;</li></ul>\n";

        WikiToHtmlFormatter formatter = new WikiToHtmlFormatter();
        String formattedText = formatter.format(text);

        assertEquals("basic formatting", expectedText, formattedText);
    }
}
