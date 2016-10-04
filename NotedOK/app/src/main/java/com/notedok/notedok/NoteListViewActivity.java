package com.notedok.notedok;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class NoteListViewActivity extends AppCompatActivity implements MasterActivity {
    private RecyclerView _notesView;
    private SwipeRefreshLayout _swipeRefreshLayout;
    // Protects from re-loading the notes every time the activity resumes
    private boolean _notesLoaded;

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
        _notesView = (RecyclerView)findViewById(R.id.notes_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _notesView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        _notesView.setLayoutManager(layoutManager);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up buttons
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: do we need this?

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: do we need this?

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: resume correctly

        // Avoid reloading all data every time user switches from and to the app.
        if (!_notesLoaded) {
            refresh();
        }
    }

    private void refresh() {
        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            OnSuccess<String[]> onSuccess = new OnSuccess<String[]>() {
                @Override
                public void call(String[] result) {
                    CurrentFileList.getInstance().reload(result);
                    loadNotes(new FileList(result));
                }
            };
            OnError onError = new OnError() {
                @Override
                public void call(Exception e) {
                    // TODO: error handling
                }
            };
            dropboxStorage.retrieveFileList(null, onSuccess, onError);
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
        // Avoid re-loading the notes next time the activity resumes
        _notesLoaded = true;
    }

    @Override
    public void switchToDetailActivity(int position) {
        Intent intent = new Intent(NoteListViewActivity.this, NoteDetailViewActivity.class);
        intent.putExtra("pos", position);
        startActivity(intent);
    }
}
