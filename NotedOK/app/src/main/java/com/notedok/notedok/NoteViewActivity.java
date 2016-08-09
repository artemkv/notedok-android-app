package com.notedok.notedok;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class NoteViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set context
        setContentView(R.layout.activity_note_view);

        // Which note is it?
        // TODO: for swipe view we probably need an index and not a path
        // TODO: Also we need to be able to access the file list
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");

        Note note = NoteCache.getInstance().getNote(path);

        // TODO: check if loaded

        // TODO: render properly
        TextView titleView = (TextView)findViewById(R.id.note_view_title);
        titleView.setText(note.Title);

        TextView textView = (TextView)findViewById(R.id.note_view_text);
        textView.setText(note.Text);
    }
}
