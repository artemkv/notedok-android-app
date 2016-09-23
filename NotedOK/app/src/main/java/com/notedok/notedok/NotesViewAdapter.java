package com.notedok.notedok;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NotesViewAdapter extends RecyclerView.Adapter<NotesViewAdapter.NoteViewHolder> {
    private int _visibleNotesTotal = 0;

    // TODO: Why static class?
    public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView _cardView;
        private int _position;

        public NoteViewHolder(View view) {
            super(view);
            _cardView = (CardView)view.findViewById(R.id.note_view);
        }

        public void bindToNote(int position) {
            _position = position;

            Note note = NoteCache.getInstance().getNote(CurrentFileList.getInstance().getPath(position));

            TextView TitleTextView = (TextView)_cardView.findViewById(R.id.note_title);
            TitleTextView.setText(note.Title);

            TextView TextTextView = (TextView)_cardView.findViewById(R.id.note_text);
            TextTextView.setText(note.Text);

            _cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            showDetails();
        }

        private void showDetails() {
            MasterActivity masterActivity = MasterActivityProvider.getMasterActivity();
            masterActivity.switchToDetailActivity(_position);
        }
    }

    public NotesViewAdapter() {
        allowOneMorePage();
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
     * @param position The position of the element
     */
    @Override
    public void onBindViewHolder(NoteViewHolder viewHolder, int position) {
        final NoteViewHolder viewHolderLocal = viewHolder;
        final int positionLocal = position;

        final Note note = NoteCache.getInstance().getNote(CurrentFileList.getInstance().getPath(positionLocal));
        viewHolderLocal.bindToNote(positionLocal);

        if (!note.IsLoaded) {
            OnSuccess<String> onSuccess = new OnSuccess<String>() {
                @Override
                public void call(String result) {
                    note.Text = result;
                    note.IsLoaded = true;
                    viewHolderLocal.bindToNote(positionLocal);
                    viewHolderLocal.itemView.setHasTransientState(false);
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
                viewHolderLocal.itemView.setHasTransientState(true);
                dropboxStorage.getNoteContent(note, onSuccess, onError);
            }

            // If reached the last visible note, load the next five
            if (positionLocal == _visibleNotesTotal - 1) {
                allowOneMorePage();
            }
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

    private void allowOneMorePage() {
        _visibleNotesTotal += 5; // TODO: constant
        if (_visibleNotesTotal > CurrentFileList.getInstance().length()) {
            _visibleNotesTotal = CurrentFileList.getInstance().length();
        }
    }
}
