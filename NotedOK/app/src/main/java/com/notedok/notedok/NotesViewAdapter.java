package com.notedok.notedok;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NotesViewAdapter extends RecyclerView.Adapter<NotesViewAdapter.NoteViewHolder> {
    private String[] _fileList;
    private int _visibleNotesTotal = 0;

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public CardView CardView;

        public NoteViewHolder(View view) {
            super(view);
            CardView = (CardView)view.findViewById(R.id.note_view);
        }

        public void bindToNote(Note note) {

            TextView TitleTextView = (TextView)CardView.findViewById(R.id.note_title);
            TitleTextView.setText(note.Title);

            TextView TextTextView = (TextView)CardView.findViewById(R.id.note_text);
            TextTextView.setText(note.Text);
        }
    }

    public NotesViewAdapter(String[] fileList) {
        if (fileList == null) {
            throw new IllegalArgumentException("fileList");
        }
        _fileList = fileList;
        loadNextPage();
    }

    public void loadNextPage() {
        _visibleNotesTotal += 5; // TODO: constant
        if (_visibleNotesTotal > _fileList.length) {
            _visibleNotesTotal = _fileList.length;
        }
    }

    /**
     * Creates new views (invoked by the layout manager)
     */
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_view, parent, false);
        NoteViewHolder viewHolder = new NoteViewHolder(view);
        return viewHolder;
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     * @param viewHolder The instance of the view holder
     * @param index The index of the element
     */
    @Override
    public void onBindViewHolder(NoteViewHolder viewHolder, int index) {
        final NoteViewHolder viewHolderLocal = viewHolder;

        // TODO: think it better, when and who constructs notes, where to store them
        final Note note = new Note();
        note.Path = _fileList[index];
        note.Title = note.Path; // TODO: make the title from the path
        note.Text = "loading..."; // TODO: ?

        viewHolderLocal.bindToNote(note);

        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                note.Text = result;
                viewHolderLocal.bindToNote(note);
            }
        };
        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
            }
        };

        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            dropboxStorage.getNoteContent(note, onSuccess, onError);
        }

        // If reached the last visible note, load the next five
        if (index == _visibleNotesTotal - 1) {
            loadNextPage();
            // This call is apparently not needed. Moreover, when called throws an exception
            // as it is busy at this moment scrolling/re-drawing the view.
            //this.notifyDataSetChanged();
        }
    }

    /**
     * Returns the size of the dataset (invoked by the layout manager)
     * @return The size of the dataset
     */
    @Override
    public int getItemCount() {
        return _visibleNotesTotal;
    }
}
