package com.notedok.notedok;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.util.LinkedList;
import java.util.List;

public class DropboxAsyncTask extends AsyncTask<Void, Void, String[]> {
    private final DbxClientV2 _dropboxClient;
    private final Callback _callback;
    private Exception _exception;

    public interface Callback {
        void onComplete(String[] result);
        void onError(Exception e);
    }

    public DropboxAsyncTask(DbxClientV2 dropboxClient, Callback callback) {
        if (dropboxClient == null) {
            throw new IllegalArgumentException("dropboxClient");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback");
        }

        _dropboxClient = dropboxClient;
        _callback = callback;
    }

    @Override
    protected void onPostExecute(String[] x) {
        super.onPostExecute(x);
        if (_exception != null) {
            _callback.onError(_exception);
        } else {
            _callback.onComplete(x);
        }
    }

    @Override
    protected String[] doInBackground(Void... params) {
        try {
            List<String> filePaths = new LinkedList<>();
            ListFolderResult result = _dropboxClient.files().listFolder("");
            List<Metadata> metadata = result.getEntries();
            for (int i = 0; i < metadata.size(); i++) {
                filePaths.add(metadata.get(i).getName());
            }
            return filePaths.toArray(new String[0]);
        } catch (DbxException e) {
            _exception = e;
        }
        return null;
    }
}
