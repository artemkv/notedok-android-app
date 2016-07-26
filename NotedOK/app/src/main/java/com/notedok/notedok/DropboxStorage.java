package com.notedok.notedok;

import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

import java.util.LinkedList;
import java.util.List;

public class DropboxStorage {
    private DbxClientV2 _dropboxClient;

    public DropboxStorage(DbxClientV2 dropboxClient) {
        if (dropboxClient == null) {
            throw new IllegalArgumentException("dropboxClient");
        }

        _dropboxClient = dropboxClient;
    }

    public List<String> retrieveFileList() {
        List<String> files = new LinkedList<String>();

        files.add("/note1.txt");
        files.add("/note2.txt");
        files.add("/note3.txt");

        try {
            // TODO: continue...
            // TODO: calling client before the authentication was confirmed will crash the app
            //ListFolderResult result = _dropboxClient.files().listFolder("/");
        } catch (/*Dbx*/Exception e) {
            // TODO: does not seem to catch anything, just crashes
            Log.i("DropboxAccess", "Error getting list of files", e);
        }

        return files;
    }
}
