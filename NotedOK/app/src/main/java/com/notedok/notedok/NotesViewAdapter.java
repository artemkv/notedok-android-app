package com.notedok.notedok;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NotesViewAdapter extends RecyclerView.Adapter<NotesViewAdapter.NoteViewHolder> {
    private String[] _fileList;

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public CardView CardView;
        public TextView TitleTextView;
        public TextView TextTextView;

        public NoteViewHolder(View view) {
            super(view);

            CardView = (CardView)view.findViewById(R.id.note_view);
            TitleTextView = (TextView)view.findViewById(R.id.note_title);
            TextTextView = (TextView)view.findViewById(R.id.note_text);
        }
    }

    public NotesViewAdapter(String[] fileList) {
        if (fileList == null) {
            throw new IllegalArgumentException("fileList");
        }
        _fileList = fileList;
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

        // TODO: Render the whole note
        // TODO: think it better, when and who constructs notes, where to store them
        Note note = new Note();
        note.Path = _fileList[index];

        viewHolderLocal.CardView.setHasTransientState(true);
        viewHolderLocal.TitleTextView.setText(_fileList[index]);
        viewHolderLocal.TextTextView.setText("loading...");

        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                viewHolderLocal.TextTextView.setText(result); // TODO: use method instead of setting public field
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
    }

    /**
     * Returns the size of the dataset (invoked by the layout manager)
     * @return The size of the dataset
     */
    @Override
    public int getItemCount() {
        return _fileList.length;
    }
}
