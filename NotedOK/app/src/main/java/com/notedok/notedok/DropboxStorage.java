package com.notedok.notedok;

import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Abstracts actual calls to Dropbox client.
 * Provides business-logic oriented high-level methods.
 */
public class DropboxStorage {
    private DbxClientV2 _dropboxClient;

    public DropboxStorage(DbxClientV2 dropboxClient) {
        if (dropboxClient == null) {
            throw new IllegalArgumentException("dropboxClient");
        }

        _dropboxClient = dropboxClient;
    }

    public List<String> retrieveFileList() {
        List<String> files = new LinkedList<>();

        files.add("/note1.txt");
        files.add("/note2.txt");
        files.add("/note3.txt");

        new DropboxAsyncTask(_dropboxClient, new DropboxAsyncTask.Callback() {
            @Override
            public void onComplete(String[] result) {
            }

            @Override
            public void onError(Exception e) {
            }
        }).execute();

        return files;
    }
}
