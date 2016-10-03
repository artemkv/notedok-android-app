package com.notedok.notedok;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NoteListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // TODO: Why static class?
    /**
     * Implements holder for the note view
     */
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
            TitleTextView.setText(note.getTitle());

            TextView TextTextView = (TextView)_cardView.findViewById(R.id.note_text);
            TextTextView.setText(note.getText());

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

    // TODO: Why static class?
    /**
     * Implements holder for the note loading indicator view
     */
    public static class NoteLoadingIndicatorViewHolder extends RecyclerView.ViewHolder {
        public NoteLoadingIndicatorViewHolder(View view) {
            super(view);
            ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.note_view_loading_progress_bar);
            progressBar.setIndeterminate(true);
        }
    }

    // Constants

    private final int NOTE_VIEW = 0;
    private final int LOADING_INDICATOR_VIEW = 1;

    // Private variables

    private int _visibleNotesTotal = 0; // Counts only real notes, not a loading indicator!

    public NoteListViewAdapter() {
        allowOneMorePage();
    }

    @Override
    /**
     * Return the view type of the item (invoked by the layout manager)
     */
    public int getItemViewType(int position) {
        if (position < _visibleNotesTotal) {
            return NOTE_VIEW;
        }

        // We are behind the last visible note, so we are looking at the progress indicator
        return LOADING_INDICATOR_VIEW;
    }

    /**
     * Creates new views (invoked by the layout manager)
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a note view or the note loading indicator view, depending on the view type
        if (viewType == NOTE_VIEW) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_view, parent, false);
            NoteViewHolder viewHolder = new NoteViewHolder(view);
            return viewHolder;
        }
        else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_loading_indicator_view, parent, false);
            NoteLoadingIndicatorViewHolder viewHolder = new NoteLoadingIndicatorViewHolder(view);
            return viewHolder;
        }
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     * @param viewHolder The instance of the view holder
     * @param position The position of the element
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof NoteViewHolder) {
            bindViewHolder((NoteViewHolder)viewHolder, position);
        }
        else {
            bindViewLoadingHolder((NoteLoadingIndicatorViewHolder)viewHolder, position);
        }
    }

    /**
     * Returns the size of the dataset (invoked by the layout manager)
     * @return The size of the dataset
     */
    @Override
    public int getItemCount() {
        // Counts every visible note + loading indicator (if necessary)

        boolean showProgressIndicator;
        if (CurrentFileList.getInstance().length() > _visibleNotesTotal) {
            showProgressIndicator = true;
        }
        else {
            showProgressIndicator = false;
        }

        return _visibleNotesTotal + (showProgressIndicator ? 1 : 0);
    }

    private void bindViewHolder(NoteViewHolder viewHolder, int position) {
        final NoteViewHolder viewHolderLocal = viewHolder;
        final int positionLocal = position;
        final int listVersionLocal = CurrentFileList.getInstance().getVersion();

        final Note note = NoteCache.getInstance().getNote(CurrentFileList.getInstance().getPath(positionLocal));
        viewHolderLocal.bindToNote(positionLocal);

        if (!note.getIsLoaded()) {
            OnSuccess<String> onSuccess = new OnSuccess<String>() {
                @Override
                public void call(String result) {
                    note.setText(result);
                    note.setIsLoaded(true);

                    // Only re-draw if the list was not reloaded
                    if (listVersionLocal == CurrentFileList.getInstance().getVersion()) {
                        viewHolderLocal.bindToNote(positionLocal);
                    }

                    // Now this view has been fully loaded, allow re-using it
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
                viewHolderLocal.itemView.setHasTransientState(true); // Do not allow re-using this view until it is fully loaded
                dropboxStorage.getNoteContent(note, onSuccess, onError);
            }
        }
    }

    private void bindViewLoadingHolder(NoteLoadingIndicatorViewHolder viewHolder, int position) {
        // TODO: this needs reworking
        AsyncWorkerTask.Worker<String> worker = new AsyncWorkerTask.Worker<String>() {
            @Override
            public String getResult() {
                return "";
            }
        };
        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                allowOneMorePage();
            }
        };

        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                // TODO: error handling
            }
        };
        new AsyncWorkerTask<String>(worker, onSuccess, onError).execute();
    }

    // Cannot be called while re-drawing!
    private void allowOneMorePage() {
        _visibleNotesTotal += 5; // TODO: constant
        if (_visibleNotesTotal > CurrentFileList.getInstance().length()) {
            _visibleNotesTotal = CurrentFileList.getInstance().length();
        }
        notifyDataSetChanged();
    }
}
