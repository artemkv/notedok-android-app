package com.notedok.notedok;

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

public class NoteListActivity extends AppCompatActivity {
    private RecyclerView _notesView;
    private SwipeRefreshLayout _swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Dropbox authentication
        DropboxStorageProvider.initialize(NoteListActivity.this);

        // Set context
        setContentView(R.layout.activity_note_list);

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

        // Refresh on pull
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        refresh();
    }

    private void refresh() {
        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            OnSuccess<String[]> onSuccess = new OnSuccess<String[]>() {
                @Override
                public void call(String[] result) {
                    loadNotes(result);
                }
            };
            OnError onError = new OnError() {
                @Override
                public void call(Exception e) {
                }
            };
            dropboxStorage.retrieveFileList(null, onSuccess, onError);
        }
    }

    private void loadNotes(String[] fileList) {
        NotesViewAdapter notesViewAdapter = new NotesViewAdapter(fileList);
        _notesView.setAdapter(notesViewAdapter);
        _swipeRefreshLayout.setRefreshing(false);
    }
}
