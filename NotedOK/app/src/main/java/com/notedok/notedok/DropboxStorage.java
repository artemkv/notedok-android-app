package com.notedok.notedok;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

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

    public void retrieveFileList(String searchString, OnSuccess<String[]> onSuccess, OnError onError) {
        final String searchStringLocal = searchString;

        AsyncWorkerTask.Worker<String[]> worker = new AsyncWorkerTask.Worker<String[]>() {
            @Override
            public String[] getResult() {
                try {
                    List<String> filePaths = new LinkedList<>();

                    // TODO: or empty string
                    if (searchStringLocal == null) {
                        ListFolderResult result = _dropboxClient.files().listFolder("");
                        List<Metadata> metadata = result.getEntries();
                        for (int i = 0; i < metadata.size(); i++) {
                            filePaths.add(metadata.get(i).getName());
                        }
                    } else {
                        filePaths.add("search results");
                    }

                    return filePaths.toArray(new String[0]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String[]>(worker, onSuccess, onError).execute();
    }
}