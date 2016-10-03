package com.notedok.notedok;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Represents the fragment that is used to show the note detail view.
 */
public class NoteDetailViewPageFragment extends Fragment {
    public static final String POSITION_ARGUMENT_NAME = "pos";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create a view to show the note
        View noteDetailView = inflater.inflate(R.layout.fragment_note_detail_view, container, false);

        // Which note is it?
        Bundle args = getArguments();
        int position = args.getInt(NoteDetailViewPageFragment.POSITION_ARGUMENT_NAME);

        // Get the note
        Note note = NoteCache.getInstance().getNote(CurrentFileList.getInstance().getPath(position));

        // TODO: check if loaded, and load if not

        // Render the note
        // TODO: render properly
        TextView titleView = (TextView)noteDetailView.findViewById(R.id.note_view_title);
        titleView.setText(note.getTitle());

        TextView textView = (TextView)noteDetailView.findViewById(R.id.note_view_text);
        textView.setText(note.getText());

        // Return the prepared view
        return noteDetailView;
    }
}
