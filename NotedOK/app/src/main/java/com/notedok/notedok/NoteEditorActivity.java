package com.notedok.notedok;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

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

        // Set up recycler view
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

            _titleEditor.setText(note.getTitle());
            _textEditor.setText(note.getText());
        }
        else
        {
            // TODO: this is new note or what
        }
    }
}
