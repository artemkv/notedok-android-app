package com.notedok.notedok;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
                    List<String> filePaths = new LinkedList<>(); // TODO: consider ArrayList

                    // TODO: or empty string
                    if (searchStringLocal == null) {
                        ListFolderResult result = _dropboxClient.files().listFolder("");
                        List<Metadata> metadata = result.getEntries();

                        List<FileMetadata> fileMetadata = new LinkedList<>();
                        for (int i = 0; i < metadata.size(); i++) {
                            Metadata itemMetadata = metadata.get(i);
                            if (Metadata.class.isInstance(itemMetadata) && itemMetadata.getPathLower().endsWith(".txt")) {
                                fileMetadata.add((FileMetadata)itemMetadata);
                            }
                        }
                        // TODO: fileMetadata.sort(

                        for (int i = 0; i < fileMetadata.size(); i++) {
                            filePaths.add("/" + fileMetadata.get(i).getName());
                        }
                    } else {
                        filePaths.add("search results");
                    }

                    return filePaths.toArray(new String[0]);
                } catch (DbxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String[]>(worker, onSuccess, onError).execute();
    }

    public void getNoteContent(Note note, OnSuccess<String> onSuccess, OnError onError) {
        final Note noteLocal = note;

        AsyncWorkerTask.Worker<String> worker = new AsyncWorkerTask.Worker<String>() {
            @Override
            public String getResult() {
                try {
                    OutputStream stream = new ByteArrayOutputStream();
                    _dropboxClient.files().download(noteLocal.Path).download(stream);
                    return stream.toString();
                } catch (DbxException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String>(worker, onSuccess, onError).execute();
    }
}
