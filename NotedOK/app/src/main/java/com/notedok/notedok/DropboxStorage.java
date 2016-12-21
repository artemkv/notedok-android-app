package com.notedok.notedok;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.SearchBuilder;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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

    /**
     * Retrieves the note text as a string.
     * The note is supposed to have Path property set to file path in format "/my file.txt" (exactly as retrieved by retrieveFileList).
     * Dropbox API returns the file content as is, in the form of binary stream.
     * The method is supposed to convert whatever encoding was used in the file to UTF-16 string.
     * @param note Note to retrieve the content for
     * @param onSuccess The callback that is to be called on success.
     * @param onError The callback that is to be called on error.
     */
    public void getNoteContent(Note note, OnSuccess<String> onSuccess, OnError onError) {
        final Note noteLocal = note;

        AsyncWorkerTask.Worker<String> worker = new AsyncWorkerTask.Worker<String>() {
            @Override
            public String getResult() {
                try {
                    OutputStream stream = new ByteArrayOutputStream();
                    _dropboxClient.files().download(noteLocal.getPath()).download(stream);
                    String content = stream.toString();
                    stream.close();
                    return content;
                } catch (DbxException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String>(worker, onSuccess, onError).execute();
    }

    /**
     * Renames the note by changing the corresponding file path to the newPath. The file paths are in the format "/my file.txt".
     * File paths are taken as the are, no additional processing is done by this method. Business code is supposed to be able to properly convert the note title to the file path by its own means.
     * That means that the newPath is supposed to be file system-friendly, and don't use any special characters that are not allowed by any existing file system.
     * In practice that means it should not contain any of the following characters: /?<>\:*|"^
     * The file with oldPath is supposed to exist, ot the error will be returned.
     * If the note with newPath already exists, the method will return a non-specific error. The caller is supposed to enforce the uniqueness of the file by its own means and try again.
     * Uniqueness can be ensured by applying the timestamp to the file path, i.e. "/my file~~1426963430173.txt"
     * @param oldPath The old path of the file with the note
     * @param newPath The new path of the file with the note
     * @param onSuccess The callback that is to be called on success.
     * @param onError The callback that is to be called on error.
     */
    public void renameNote(String oldPath, String newPath, OnSuccess<String> onSuccess, OnError onError) {
        final String oldPathLocal = oldPath;
        final String newPathLocal = newPath;

        AsyncWorkerTask.Worker<String> worker = new AsyncWorkerTask.Worker<String>() {
            @Override
            public String getResult() {
                try {
                    _dropboxClient.files().move(oldPathLocal, newPathLocal);
                    return newPathLocal;
                } catch (DbxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String>(worker, onSuccess, onError).execute();
    }

    /**
     * Saves the note text to the file with path specified in the Path property.
     * The note is supposed to have Path property set to file path in format "/my file.txt" (exactly as retrieved by retrieveFileList).
     * Dropbox API treats the passed data as a binary stream and saves it to the file without any extra processing.
     * The method converts the UTF-16 string to the byte array using UTF-8 encoding.
     *
     * If the note with the same path already exists, parameter "overwrite" controls the method behavior.
     * For a new note, overwrite should be set to false to avoid replacing the existing note. If the note with the same path already exists, this method will auto-rename the note.
     * Auto-rename is implemented by means of Dropbox API according their auto-rename strategy.
     * To avoid that the new note changes its title after saving, caller is supposed to analyze the returned path, detect auto-rename and rename it again ensuring the uniqueness by its own means.
     * Uniqueness can be ensured by applying the timestamp to the file path, i.e. "/my file~~1426963430173.txt"
     *
     * For an existing note, overwrite should be set to true, to avoid creating a second copy of the same note.
     *
     * When restoring a deleted note, overwrite should be set to false, to avoid replacing the existing note. If the note with the same path already exists, this method will auto-rename the note.
     * It is OK for the restored note to keep the auto-renamed path.
     *
     * Empty path is not allowed. If the note title is empty, the caller is supposed to ensure the path is non-empty, by applying the timestamp to the file path, i.e. "/~~1426963430173.txt"
     * When note is saved successfully, this method returns the actual path the note is saved to.
     * @param note Note to save
     * @param overwrite When the note at the specified path already exists, whether it should be overwritten or saved with the new name.
     * @param onSuccess The callback that is to be called on success.
     * @param onError The callback that is to be called on error.
     */
    public void saveNote(Note note, boolean overwrite, OnSuccess<String> onSuccess, OnError onError) {
        final Note noteLocal = note;
        final boolean overwriteLocal = overwrite;

        AsyncWorkerTask.Worker<String> worker = new AsyncWorkerTask.Worker<String>() {
            @Override
            public String getResult() {
                try {
                    UploadBuilder uploadBuilder = _dropboxClient.files().uploadBuilder(noteLocal.getPath());

                    // TODO: when file size is 0 bytes, add + auto-rename is not applied. Maybe check with Dropbox support
                    if (overwriteLocal) {
                        uploadBuilder.withMode(WriteMode.OVERWRITE);
                    } else {
                        uploadBuilder.withMode(WriteMode.ADD).withAutorename(true);
                    }

                    // TODO: is there byte order mark?
                    InputStream stream = new ByteArrayInputStream(noteLocal.getText().getBytes("UTF-8"));
                    FileMetadata fileMetadata = uploadBuilder.uploadAndFinish(stream);
                    stream.close();

                    return "/" + fileMetadata.getName();
                } catch (DbxException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String>(worker, onSuccess, onError).execute();
    }

    /**
     * Deletes the note.
     * The note is supposed to have Path property set to file path in format "/my file.txt" (exactly as retrieved by retrieveFileList).
     * @param note note to delete
     * @param onSuccess The callback that is to be called on success.
     * @param onError The callback that is to be called on error.
     */
    public void deleteNote(Note note, OnSuccess<String> onSuccess, OnError onError) {
        final Note noteLocal = note;

        AsyncWorkerTask.Worker<String> worker = new AsyncWorkerTask.Worker<String>() {
            @Override
            public String getResult() {
                try {
                    _dropboxClient.files().delete(noteLocal.getPath());
                    return "";
                } catch (DbxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new AsyncWorkerTask<String>(worker, onSuccess, onError).execute();
    }
}
