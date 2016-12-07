package com.notedok.notedok;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Represents the fragment that is used to show the note detail view.
 */
public class NoteDetailViewPageFragment extends Fragment {
    public static final String FILES_ARGUMENT_NAME = "files";
    public static final String POSITION_ARGUMENT_NAME = "pos";

    private ProgressBar _loadingIndicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View noteDetailView = inflater.inflate(R.layout.fragment_note_detail_view, container, false);

        // Set up the progress bar
        _loadingIndicator = (ProgressBar) noteDetailView.findViewById(R.id.note_view_loading_indicator);

        // Which note is it?
        Bundle args = getArguments();
        FileList fileList = new FileList(args.getStringArrayList(NoteDetailViewPageFragment.FILES_ARGUMENT_NAME));
        int position = args.getInt(NoteDetailViewPageFragment.POSITION_ARGUMENT_NAME);

        // Get the note
        final Note note = NoteCache.getInstance().getNote(fileList.getPath(position));

        // Render - sync or async
        renderNoteTitle(note, noteDetailView);
        if (note.getIsLoaded()) {
            renderNoteText(note, noteDetailView);
        } else {
            renderNoteTextAsync(note, noteDetailView);
        }

        // Return the prepared view
        return noteDetailView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: resume correctly
    }

    private void renderNoteTitle(Note note, View noteDetailView) {
        WebView webView = (WebView) noteDetailView.findViewById(R.id.note_view_text);

        webView.loadDataWithBaseURL(null,
                wrapHtml(webView.getContext(), renderNoteTitleHtml(note.getTitle()), ""),
                "text/html",
                "UTF-8",
                null);
    }

    private void renderNoteTextAsync(Note note, View noteDetailView) {
        _loadingIndicator.setVisibility(View.VISIBLE);

        final Note noteLocal = note;
        final View noteDetailViewLocal = noteDetailView;

        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                _loadingIndicator.setVisibility(View.GONE);

                noteLocal.setText(result);
                noteLocal.setIsLoaded(true);

                // TODO: can this view be re-used?
                // TODO: basically, does it need to verify that the position is still the same?
                renderNoteText(noteLocal, noteDetailViewLocal);
            }
        };
        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                // TODO: error handling
            }
        };

        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            dropboxStorage.getNoteContent(note, onSuccess, onError);
        }
    }

    private void renderNoteText(Note note, View noteDetailView) {
        WebView webView = (WebView) noteDetailView.findViewById(R.id.note_view_text);

        String formattedText;
        try {
            formattedText = renderNoteTextHtml(note.getText());
        } catch (RuntimeException e) {
            Log.e("NoteWebView", "Could not render note " + note.getPath(), e);
            formattedText = note.getText();
        }

        webView.loadDataWithBaseURL(null,
            wrapHtml(webView.getContext(), renderNoteTitleHtml(note.getTitle()), formattedText),
            "text/html",
            "UTF-8",
            null);
    }

    private String wrapHtml(Context context, String titleHtml, String textHtml) {
        return context.getString(R.string.note_web_view_wrapping_html, titleHtml, textHtml);
    }

    private String renderNoteTitleHtml(String text) {
        if (text != null && text.length() > 0) {
            return  text;
        }
        return "<span class='placeholder'>" + getString(R.string.note_no_title) + "</span>";
    }

    private String renderNoteTextHtml(String text) {
        // replace '[http' with '[rmhttp'
        text = text.replaceAll("(\\[http)", "[rmhttp");

        // put link in square brackets
        text = text.replaceAll("(\\bhttps?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", "[$1]");

        // replace '[rmhttp' with '[http'
        text = text.replaceAll("(\\[rmhttp)", "[http");

        WikiToHtmlFormatter formatter = new WikiToHtmlFormatter();
        return formatter.format(text);
    }
}
