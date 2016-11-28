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

    private EditText _titleEditor;
    private EditText _textEditor;

    private ArrayList<String> _files;
    private int _position;
    private Note _note;
    private boolean _isNew;

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
            setTitle(R.string.note_editor_existing_note_activity_title);

            // Get the note
            FileList fileList = new FileList(_files);
            _note = NoteCache.getInstance().getNote(fileList.getPath(_position));

            // TODO: if !note.getIsLoaded(), load async with progress bar

            _titleEditor.setText(_note.getTitle());
            _textEditor.setText(_note.getText());

            _isNew = false;
        }
        else
        {
            setTitle(R.string.note_editor_new_note_activity_title);

            // New note
            _note = new Note();
            _note.setTitle("");
            _note.setText("");
            _note.setIsLoaded(true);

            _isNew = true;
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
                // TODO: if modified, ask whether to save changes
                this.finish();
                return true;
            case R.id.action_save:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Saves the complete note, new or existing.
     */
    private void saveNote() {
        // TODO: only allow saving note that is fully loaded. Can we disable save icon completely before the note is loaded?
        if (_note.getIsLoaded()) {
            String newTitle = _titleEditor.getText().toString(); // TODO: 50 char max
            String newText = _textEditor.getText().toString();

            _note.setTitle(newTitle);
            _note.setText(newText);

            if (_isNew) {
                // Note is not yet in the cache, so it is safe to set path
                _note.setPath(TitleToPathConverter.getInstance().generatePath(_note.getTitle(), false));
                saveNewNote();
            }
            else {
                // TODO: cache might get invalid, if path changes
                // TODO: only if changed ?
                saveExistingNote();
            }
        }
    }

    private void saveExistingNote() {
        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                // Remember the final path
                // TODO: invalidate cache
                _note.setPath(result);

                // Continue with the note content
                saveExistingNoteText();
            }
        };
        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                // Try again with unique title
                saveExistingNoteWithUniqueTitle();
            }
        };

        // First save the title
        String newPath = TitleToPathConverter.getInstance().generatePath(_note.getTitle(), false);
        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        dropboxStorage.renameNote(_note.getPath(), newPath, onSuccess, onError);
    }

    private void saveExistingNoteWithUniqueTitle() {
        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                // Remember the final path
                // TODO: invalidate cache
                _note.setPath(result);

                // Continue with the note content
                saveExistingNoteText();
            }
        };
        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                // TODO: error handling
            }
        };

        // So we could not save the note with the previous title, try make the title unique and save again
        String newPath = TitleToPathConverter.getInstance().generatePath(_note.getTitle(), true);
        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        dropboxStorage.renameNote(_note.getPath(), newPath, onSuccess, onError);
    }

    private void saveExistingNoteText() {
        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                // For the existing note, we are done
                finishEditing();
            }
        };
        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                // TODO: error handling
            }
        };

        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        dropboxStorage.saveNote(_note, true, onSuccess, onError);
    }

    private void saveNewNote() {
        OnSuccess<String> onSuccess = new OnSuccess<String>() {
            @Override
            public void call(String result) {
                handleNewNoteAutoRenamingByDropbox(result);
            }
        };
        OnError onError = new OnError() {
            @Override
            public void call(Exception e) {
                // TODO: error handling
            }
        };

        DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
        dropboxStorage.saveNote(_note, false, onSuccess, onError);
    }

    /**
     * Handles the situation when new note was created with the same title as some existing note
     * Because of overwrite=false we pass when saving the new note, dropbox will rename it to ensure the name is unique
     * We have to catch it and ensure uniqueness by our own means
     * @param path The new path, generated by Dropbox
     */
    private void handleNewNoteAutoRenamingByDropbox(String path) {
        // Always update the note path to the actual value to be able to rename it in the future
        // Note is not yet in the cache, so it is safe to set path
        _note.setPath(path);

        // Check whether dropbox renamed the note to ensure uniqueness
        String newTitle = TitleToPathConverter.getInstance().getTitle(_note.getPath());
        if (!_note.getTitle().equals(newTitle)) {
            // Force the unique title from the user-provided title
            String newPath = TitleToPathConverter.getInstance().generatePath(_note.getTitle(), true);

            OnSuccess<String> onSuccess = new OnSuccess<String>() {
                @Override
                public void call(String result) {
                    // Remember the final path
                    // Note is not yet in the cache, so it is safe to set path
                    _note.setPath(result);

                    // Now we are also done for the new note
                    finishEditing();
                }
            };
            OnError onError = new OnError() {
                @Override
                public void call(Exception e) {
                    // TODO: error handling
                }
            };

            DropboxStorage dropboxStorage = DropboxStorageProvider.getDropboxStorage();
            dropboxStorage.renameNote(_note.getPath(), newPath, onSuccess, onError);
        }
        else {
            // Nothing else to do for the new note
            finishEditing();
        }
    }

    /**
     * This method finishes the editing activity and goes back to the previous one.
     * !Make sure to not have any async actions pending.
     */
    private void finishEditing() {
        // TODO: this is hardcoded
        Context context = getApplicationContext();
        CharSequence text = "Saved";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        // Go back to the previous activity
        this.finish();
    }
}
