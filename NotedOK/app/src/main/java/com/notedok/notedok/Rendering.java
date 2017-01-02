package com.notedok.notedok;

import android.content.Context;
import android.widget.Toast;

public final class Rendering {
    /**
     * Renders the html from the note text.
     * @param text Note text.
     * @return Note Html
     */
    public static String renderNoteTextHtml(String text) {
        // replace '[http' with '[rmhttp'
        text = text.replaceAll("(\\[http)", "[rmhttp");

        // put link in square brackets
        text = text.replaceAll("(\\bhttps?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", "[$1]");

        // replace '[rmhttp' with '[http'
        text = text.replaceAll("(\\[rmhttp)", "[http");

        WikiToHtmlFormatter formatter = new WikiToHtmlFormatter();
        return formatter.format(text);
    }

    /**
     * Renders the reduced html from the note text.
     * @param text Note text.
     * @return Note Html
     */
    // TODO: code duplication
    public static String renderNoteTextHtmlReduced(String text) {
        // replace '[http' with '[rmhttp'
        text = text.replaceAll("(\\[http)", "[rmhttp");

        // put link in square brackets
        text = text.replaceAll("(\\bhttps?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", "[$1]");

        // replace '[rmhttp' with '[http'
        text = text.replaceAll("(\\[rmhttp)", "[http");

        WikiToHtmlFormatter formatter = new WikiToHtmlFormatter();
        return formatter.formatReduced(text);
    }

    /**
     * Escapes the HTML unsafe tags. This method does not guarantee safety!
     * This method only makes sure that the basic tags are converted before formatting.
     * If any unsafe tags are left in the end, they should be sanitized by the UI.
     * @param unsafeText Original text.
     * @return Escaped text.
     */
    public static String htmlEscape(String unsafeText) {
        String safeText = unsafeText
                .replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#39;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

        return  safeText;
    }

    /**
     * Shows the dropbox connection error message.
     */
    public static void showDropboxError() {
        int duration = Toast.LENGTH_SHORT;
        Context context = MyApplication.getAppContext();
        Toast toast = Toast.makeText(context, context.getText(R.string.dropbox_error), duration);
        toast.show();
    }
}
