package com.notedok.notedok;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteListViewActivity extends AppCompatActivity implements MasterActivity {
    private static final int CREATE_NEW_NOTE_REQUEST_CODE = 1;
    private static final int SHOW_NOTE_DETAILS_REQUEST_CODE = 2;

    private RecyclerView _notesView;
    private SwipeRefreshLayout _swipeRefreshLayout;
    private ProgressBar _loadingIndicator;
    private boolean _notesLoaded; // Protects from re-loading the notes every time the activity resumes
    private String _searchString;
    private TextView _emptyView;
    private FloatingActionButton _addNewNoteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Dropbox authentication
        DropboxStorageProvider.initialize(NoteListViewActivity.this);

        // Set context
        setContentView(R.layout.activity_note_list);

        // Set itself as a master activity
        MasterActivityProvider.initialize(this);

        // Set up recycler view
        _notesView = (RecyclerView) findViewById(R.id.notes_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _notesView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        _notesView.setLayoutManager(layoutManager);

        // Set up the delete on swipe
        ItemTouchHelper.SimpleCallback deleteOnSwipeTouchCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    // Not implemented since the dragDirs is 0
                    return false;
                }
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    DeletableViewHolder deletable = (viewHolder instanceof DeletableViewHolder ? (DeletableViewHolder) viewHolder : null);
                    if (deletable != null) {
                        deletable.onDelete();
                    } else {
                        // Non-deletable view, skip
                    }
                }
            };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(deleteOnSwipeTouchCallback);
        itemTouchHelper.attachToRecyclerView(_notesView);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up buttons
        _addNewNoteButton = (FloatingActionButton) findViewById(R.id.add_new_note);
        _addNewNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddNewNoteButtonClick();
            }
        });

        // Set up refresh on pull
        _swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.notes_view_swipe);
        _swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        _swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        // Set up the progress bar
        _loadingIndicator = (ProgressBar) findViewById(R.id.notes_view_loading_indicator);

        // Set up the empty view
        _emptyView = (TextView) findViewById(R.id.notes_view_empty_view);

        // Set up the error reporter
        Rendering.initializeDropboxError(getBaseContext());

        // Handle search intent
        // For example, after the screen is rotated, the activity is re-created and _searchString is null
        // So this line would restore it from the intent data
        handleSearchIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list, menu);

        // Setup search
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true; // KEEP IT TO TRUE OR IT DOESN'T OPEN !!
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Programmatically trigger the search intent with empty search
                if (_searchString != null) {
                    // Exactly the same intent as search view uses
                    Intent intent = new Intent(Intent.ACTION_SEARCH, null, getBaseContext(), NoteListViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(SearchManager.QUERY, (String) null);
                    startActivity(intent);
                }
                return true; // OR FALSE IF YOU DIDN'T WANT IT TO CLOSE!
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        if (_searchString != null) {
            MenuItemCompat.expandActionView(searchItem);
            searchView.setQuery(_searchString, false);
            searchView.clearFocus();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: do we need this? Currently commented out the layout in the menu_note_list.xml

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // TODO: if we need settings, uncomment
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Set intent so if the activity is re-created (for example, if the screen rotates),
        // we still keep the intent and can extract search string from it.
        setIntent(intent);

        // We get here after search, so we need to extract search string from the new intent
        handleSearchIntent(intent);

        // Even if notes are already loaded, force re-load
        forceReload();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If search is active, do not allow adding notes
        if (_searchString == null) {
            _addNewNoteButton.setVisibility(View.VISIBLE);
        }
        else {
            _addNewNoteButton.setVisibility(View.GONE);
        }

        // Avoid reloading all data every time user switches from and to the app or rotates the device.
        if (!_notesLoaded) {
            _notesLoaded = true;

            // Normally before refresh we need loading indicator.
            // Except the case when user refreshes on swipe, in which case there is another loading indicator.
            _loadingIndicator.setVisibility(View.VISIBLE);

            // onResume is called always, whether it is a new activity (start app/rotate screen) or just a new intent (search).
            // So as long as we extracted the search string from the intent, notes should be loaded the same way.
            refresh();
        }
    }

    private void refresh() {
        _emptyView.setVisibility(View.GONE);

        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            OnSuccess<ArrayList<String>> onSuccess = new OnSuccess<ArrayList<String>>() {
                @Override
                public void call(ArrayList<String> result) {
                    loadNotes(new FileList(result));
                }
            };
            OnError onError = new OnError() {
                @Override
                public void call(Exception e) {
                    Rendering.showDropboxError();
                }
            };
            dropboxStorage.retrieveFileList(_searchString, onSuccess, onError);
        } else {
            // Will have to re-try
            _notesLoaded = false;
        }
    }

    private void loadNotes(FileList filelist) {
        // Clear the cache to force the call to Dropbox for the latest content
        NoteCache.getInstance().clear();
        // Reset the adapter
        NoteListViewAdapter noteListViewAdapter = new NoteListViewAdapter(filelist);
        _notesView.setAdapter(noteListViewAdapter);
        // Stop the pull refresh animation
        _swipeRefreshLayout.setRefreshing(false);
        // Don't need loading indicator anymore
        _loadingIndicator.setVisibility(View.GONE);
        // Show empty view if needed
        if (filelist.getLength() == 0) {
            _emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void switchToDetailActivity(FileList fileList, int position) {
        Intent intent = new Intent(NoteListViewActivity.this, NoteDetailViewActivity.class);
        intent.putStringArrayListExtra(MasterActivity.FILES_INTENT_EXTRA_NAME, fileList.getAsArrayList());
        intent.putExtra(MasterActivity.POSITION_INTENT_EXTRA_NAME, position);
        startActivityForResult(intent, SHOW_NOTE_DETAILS_REQUEST_CODE);
    }

    @Override
    public void deleteItem(int position) {
        final int positionLocal = position;
        final NoteListViewAdapter noteListViewAdapter = (NoteListViewAdapter) _notesView.getAdapter();

        // Delete from the UI
        final String deletedNotePath = noteListViewAdapter.deleteNote(position);

        // If no more notes left, show empty view
        if (noteListViewAdapter.getItemCount() == 0) {
            _emptyView.setVisibility(View.VISIBLE);
        }

        // Delete from the Dropbox
        Note noteToDelete = NoteCache.getInstance().getNote(deletedNotePath);
        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            OnSuccess<String> onSuccess = new OnSuccess<String>() {
                @Override
                public void call(String result) {
                    setupUndo(positionLocal, deletedNotePath);
                }
            };
            OnError onError = new OnError() {
                @Override
                public void call(Exception e) {
                    Rendering.showDropboxError();
                }
            };
            dropboxStorage.deleteNote(noteToDelete, onSuccess, onError);
        }
    }

    // TODO: this is a bit scary, since the undo data comes from NoteCache. It's OK unless the cache gets cleaned
    public void restoreItem(int position, String deletedNotePath) {
        final int positionLocal = position;
        final String deletedNotePathLocal = deletedNotePath;
        final NoteListViewAdapter noteListViewAdapter = (NoteListViewAdapter) _notesView.getAdapter();

        // Restore in UI
        noteListViewAdapter.restoreNote(positionLocal, deletedNotePathLocal);

        // View cannot be empty anymore
        _emptyView.setVisibility(View.GONE);

        // Restore in Dropbox
        Note noteToRestore = NoteCache.getInstance().getNote(deletedNotePath);
        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            OnSuccess<String> onSuccess = new OnSuccess<String>() {
                @Override
                public void call(String result) {
                    // Nothing to do here
                }
            };
            OnError onError = new OnError() {
                @Override
                public void call(Exception e) {
                    Rendering.showDropboxError();
                }
            };
            dropboxStorage.saveNote(noteToRestore, false, onSuccess, onError);
        }
    }

    private void setupUndo(int position, String deletedNotePath) {
        final int positionLocal = position;
        final String deletedNotePathLocal = deletedNotePath;

        // TODO: strings to resources
        Snackbar snackbar = Snackbar
                .make(_notesView, "Note deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        restoreItem(positionLocal, deletedNotePathLocal);
                    }
                });
        snackbar.show();
    }

    /*
    Extract the search string from the search intent
     */
    private void handleSearchIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            _searchString = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    private void onAddNewNoteButtonClick() {
        Intent intent = new Intent(NoteListViewActivity.this, NoteEditorActivity.class);
        // We don't need extra data, so just start intent
        startActivityForResult(intent, CREATE_NEW_NOTE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CREATE_NEW_NOTE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                forceReload();
            }
        } else if (requestCode == SHOW_NOTE_DETAILS_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                forceReload();
            }
        }
    }

    private void forceReload() {
        // The actual reload happens only in onResume
        _notesLoaded = false;
        _notesView.setAdapter(null);
    }
}
