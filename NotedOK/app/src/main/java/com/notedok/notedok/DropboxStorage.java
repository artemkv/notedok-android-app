package com.notedok.notedok;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchBuilder;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    /**
     * Retrieves the list of files that are in the application folder. Every record in the file list is the file path in the format "/my file.txt".
     * File paths are returned as the are, no additional processing is done by this method. Business code is supposed to be able to properly convert the file path to the note title by its own means.
     * Only text files are retrieved (files that have extension ".txt").
     * Only files from the root folder are retrieved. Subfolders are ignored.
     * When no searchString is provided, all files are retrieved - the result can be used to build the auto-suggest source. * TODO: might require multiple calls when number of files grows too big
     * When searchString is provided, the number of retrieved files is limited to 1000. To get the files that are cut off, user need to provide more specific search string.
     * The search is implemented by means of Dropbox API. This method does not provide any search logic of its own.
     * The results are ordered by date.
     * @param searchString The search string. If null or empty, no filter is applies.
     * @param onSuccess The callback that is to be called on success.
     * @param onError The callback that is to be called on error.
     */
    public void retrieveFileList(String searchString, OnSuccess<ArrayList<String>> onSuccess, OnError onError) {
        final String searchStringLocal = searchString;

        AsyncWorkerTask.Worker<ArrayList<String>> worker = new AsyncWorkerTask.Worker<ArrayList<String>>() {
            @Override
            public ArrayList<String> getResult() {
                try {
                    ArrayList<String> filePaths = new ArrayList<String>();
                    List<Metadata> metadata;

                    // Get to metadata
                    if (searchStringLocal == null || searchStringLocal.length() == 0) {
                        ListFolderResult result = _dropboxClient.files().listFolder("");
                        metadata = result.getEntries();
                    } else {

                        SearchBuilder searchBuilder = _dropboxClient.files().searchBuilder("", searchStringLocal).withMaxResults(1000L);
                        SearchResult result = searchBuilder.start();
                        List<SearchMatch> matches = result.getMatches();

                        metadata = new LinkedList<>();
                        for (int i = 0; i < matches.size(); i++) {
                            metadata.add(matches.get(i).getMetadata());
                        }
                    }

                    // Filter metadata
                    List<FileMetadata> fileMetadata = new LinkedList<>();
                    for (int i = 0; i < metadata.size(); i++) {
                        Metadata itemMetadata = metadata.get(i);
                        if (Metadata.class.isInstance(itemMetadata) && itemMetadata.getPathLower().endsWith(".txt")) {
                            fileMetadata.add((FileMetadata) itemMetadata);
                        }
                    }

                    // Sort metadata
                    Collections.sort(fileMetadata, new Comparator<FileMetadata>() {
                        @Override
                        public int compare(FileMetadata a, FileMetadata b) {
                            return b.getServerModified().compareTo(a.getServerModified());
                        }
                    });

                    // Transform metadata
                    for (int i = 0; i < fileMetadata.size(); i++) {
                        filePaths.add("/" + fileMetadata.get(i).getName());
                    }

                    return filePaths;
                } catch (DbxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<ArrayList<String>>(worker, onSuccess, onError).execute();
    }

    // TODO: what if note is already deleted by then?
    public void getNoteContent(Note note, OnSuccess<String> onSuccess, OnError onError) {
        final Note noteLocal = note;

        AsyncWorkerTask.Worker<String> worker = new AsyncWorkerTask.Worker<String>() {
            @Override
            public String getResult() {
                try {
                    OutputStream stream = new ByteArrayOutputStream();
                    _dropboxClient.files().download(noteLocal.getPath()).download(stream);
                    return stream.toString();
                } catch (DbxException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String>(worker, onSuccess, onError).execute();
    }
}
