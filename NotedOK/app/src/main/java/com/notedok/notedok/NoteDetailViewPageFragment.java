package com.notedok.notedok;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Represents the fragment that is used to show the note detail view.
 */
public class NoteDetailViewPageFragment extends Fragment {
    public static final String FILES_ARGUMENT_NAME = "files";
    public static final String POSITION_ARGUMENT_NAME = "pos";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create a view to show the note
        final View noteDetailView = inflater.inflate(R.layout.fragment_note_detail_view, container, false);

        // Which note is it?
        Bundle args = getArguments();
        FileList fileList = new FileList(args.getStringArrayList(NoteDetailViewPageFragment.FILES_ARGUMENT_NAME));
        int position = args.getInt(NoteDetailViewPageFragment.POSITION_ARGUMENT_NAME);

        // Get the note
        final Note note = NoteCache.getInstance().getNote(fileList.getPath(position));

        // Render - sync or async
        if (note.getIsLoaded()) {
            renderNote(note, noteDetailView);
        }
        else {
            renderNoteTitle(note, noteDetailView);
            // TODO: progress bar

            OnSuccess<String> onSuccess = new OnSuccess<String>() {
                @Override
                public void call(String result) {
                    note.setText(result);
                    note.setIsLoaded(true);

                    // TODO: can this view be re-used?
                    // TODO: basically, does it need to verify that the position is still the same?
                    renderNote(note, noteDetailView);
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

        // Return the prepared view
        return noteDetailView;
    }

    private void renderNote(Note note, View noteDetailView) {
        // TODO: render properly
        TextView titleView = (TextView)noteDetailView.findViewById(R.id.note_view_title);
        titleView.setText(note.getTitle());

        TextView textView = (TextView)noteDetailView.findViewById(R.id.note_view_text);
        textView.setText(note.getText());
    }

    private void renderNoteTitle(Note note, View noteDetailView) {
        // TODO: render properly
        TextView titleView = (TextView) noteDetailView.findViewById(R.id.note_view_title);
        titleView.setText(note.getTitle());
    }
}