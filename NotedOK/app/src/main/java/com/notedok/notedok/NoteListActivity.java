package com.notedok.notedok;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class NoteListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Dropbox authentication
        DropboxStorageProvider.initialize(NoteListActivity.this);

        // Set context
        setContentView(R.layout.activity_note_list);

        // Set up recycler view
        RecyclerView notesView = (RecyclerView)findViewById(R.id.notes_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        notesView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        notesView.setLayoutManager(layoutManager);

        // TODO: this is fake data, replace real stuff
        String[] fileList = new String[] {
                "aaa",
                "bbb",
                "ccc",
                "ddd",
                "eee",
                "fff",
                "ggg",
                "hhh",
                "iii",
                "jjj",
                "kkk",
                "lll",
                "mmm",
                "nnn",
                "ooo",
                "ppp",
                "qqq",
                "rrr",
                "sss",
                "ttt",
                "uuu",
                "vvv",
                "www",
                "xxx",
                "yyy",
                "zzz"
        };

        NotesViewAdapter notesViewAdapter = new NotesViewAdapter(fileList);
        notesView.setAdapter(notesViewAdapter);

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

        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        if (dropboxStorage != null) {
            OnSuccess<String[]> onSuccess = new OnSuccess<String[]>() {
                @Override
                public void call(String[] result) {
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
}
