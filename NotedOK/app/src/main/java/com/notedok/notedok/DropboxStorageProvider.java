package com.notedok.notedok;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

// TODO: make singleton

/**
 * Constructs and stores an instance of DropboxStorage.
 * Handles the Dropbox authentication.
 */
public final class DropboxStorageProvider {
    // To obtain the token from Dropbox
    final static private String APP_KEY = "y9i1eshn74yuenq";

    // To cache the token
    final static private String PREFERENCES_FILE_NAME = "notedok-preferences";
    final static private String TOKEN_KEY = "dropbox-access-token";

    // Here the token is cached
    private static SharedPreferences SharedPreferences;

    // The storage that this provider provides
    private static DropboxStorage DropboxStorage;

    /**
     * Initializes the storage provider.
     * @param activity The activity that initializes a provider.
     */
    public static void initialize(AppCompatActivity activity) {
        // Save preferences
        SharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE_NAME, activity.MODE_PRIVATE);

        // If there is no token, initialize authentication flow
        String accessToken = SharedPreferences.getString(TOKEN_KEY, null);
        if (accessToken == null) {
            Auth.startOAuth2Authentication(activity, APP_KEY);
        }
    }

    /**
     * Returns the Dropbox storage to access the notes.
     * If the Dropbox storage is not initialized (authentication is not finished), returns null.
     * @return Dropbox storage if initialized; otherwise, null.
     */
    public static DropboxStorage getDropboxStorage() {
        // TODO: check whether initialized

        if (DropboxStorage == null) {
            DbxClientV2 client = getDropboxClient();
            if (client != null) {
                DropboxStorage = new DropboxStorage(client);
            }
            // TODO: else return offline storage
        }

        return DropboxStorage;
    }

    private static DbxClientV2 getDropboxClient() {
        // Try getting saved token
        String accessToken = SharedPreferences.getString(TOKEN_KEY, null);
        if (accessToken == null) {
            // No saved token, try finishing authentication
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                // Save token for the future, to avoid re-authentication
                SharedPreferences.edit().putString(TOKEN_KEY, accessToken).apply();
            }
        }

        // If authentication is not finished, there will be no token this time
        // App will have to retry again after the authentication is done
        if (accessToken != null) {
            // Funny way to obtain the client from Dropbox, copied from an example
            try {
                DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("NotedOK/1.0")
                        .withHttpRequestor(OkHttp3Requestor.INSTANCE)
                        .build();
                return new DbxClientV2(requestConfig, accessToken);
            } catch (Exception e) {
                // TODO: where to see this message?
                Log.i("DropboxAccess", "Error creating dropbox client", e);
            }
        }

        // Client could not be create - probably not yet authenticated
        return null;
    }
}