package com.notedok.notedok;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class NoteEditorActivity extends AppCompatActivity {
    public static final String FILES_INTENT_EXTRA_NAME = "files";
    public static final String POSITION_INTENT_EXTRA_NAME = "pos";

    EditText _titleEditor;
    EditText _textEditor;

    private ArrayList<String> _files;
    private int _position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set context
        setContentView(R.layout.activity_note_editor);

        // Display the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the view
        _titleEditor = (EditText)findViewById(R.id.note_editor_title);
        _textEditor = (EditText)findViewById(R.id.note_editor_text);

        // Unpack the parameters
        Intent intent = getIntent();
        _files = intent.getStringArrayListExtra(MasterActivity.FILES_INTENT_EXTRA_NAME);
        _position = intent.getIntExtra(MasterActivity.POSITION_INTENT_EXTRA_NAME, -1);

        if (_files != null && _files.size() > 0 && _position >= 0) {
            // Get the note
            FileList fileList = new FileList(_files);
            final Note note = NoteCache.getInstance().getNote(fileList.getPath(_position));

            // TODO: if !note.getIsLoaded(), load async with progress bar

            _titleEditor.setText(note.getTitle());
            _textEditor.setText(note.getText());

            setTitle("Edit note"); // TODO: resources
        }
        else
        {
            setTitle("New note"); // TODO: resources
            // TODO: this is new note or what
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // back button in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.action_save:
                // TODO: replace with actual logic
                Context context = getApplicationContext();
                CharSequence text = "Saved";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
