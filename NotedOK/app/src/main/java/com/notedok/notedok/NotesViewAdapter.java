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
    private String[] _fileList; // TODO: need to move to the common place
    private Activity _currentActivity; // TODO: need to move to the common place
    private int _visibleNotesTotal = 0;

    // TODO: Why static class?
    public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView CardView;
        private Note _note;
        private Activity _currentActivity; // TODO: need to move to the common place

        public NoteViewHolder(View view, Activity currentActivity) {
            super(view);

            _currentActivity = currentActivity;
            CardView = (CardView)view.findViewById(R.id.note_view);
        }

        public void bindToNote(Note note) {
            _note = note;

            TextView TitleTextView = (TextView)CardView.findViewById(R.id.note_title);
            TitleTextView.setText(note.Title);

            TextView TextTextView = (TextView)CardView.findViewById(R.id.note_text);
            TextTextView.setText(note.Text);

            CardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            showDetails();
        }

        private void showDetails() {
            if (_note != null) {
                Intent intent = new Intent(_currentActivity, NoteViewActivity.class);
                intent.putExtra("path", _note.Path);
                _currentActivity.startActivity(intent);
            }
        }
    }

    public NotesViewAdapter(String[] fileList, Activity currentActivity) {
        if (fileList == null)
            throw new IllegalArgumentException("fileList");
        if (currentActivity == null)
            throw new IllegalArgumentException("currentActivity");

        _fileList = fileList;
        _currentActivity = currentActivity;

        NoteCache.getInstance().clear();
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
        NoteViewHolder viewHolder = new NoteViewHolder(view, _currentActivity);
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

        final Note note = NoteCache.getInstance().getNote(_fileList[index]);
        viewHolderLocal.bindToNote(note);

        if (!note.IsLoaded) {
            OnSuccess<String> onSuccess = new OnSuccess<String>() {
                @Override
                public void call(String result) {
                    note.Text = result;
                    note.IsLoaded = true;
                    viewHolderLocal.bindToNote(note);
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

            // If reached the last visible note, load the next five
            if (index == _visibleNotesTotal - 1) {
                loadNextPage();
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
}
