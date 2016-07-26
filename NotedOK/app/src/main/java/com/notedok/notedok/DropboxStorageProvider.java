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
public class DropboxStorageProvider {
    final static private String PREFERENCES_FILE_NAME = "notedok-preferences";
    final static private String TOKEN_KEY = "dropbox-access-token";

    final static private String APP_KEY = "y9i1eshn74yuenq";

    private static SharedPreferences SharedPreferences;
    private static DropboxStorage DropboxStorage;

    public static void startOAuth2Authentication(AppCompatActivity activity) {
        SharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE_NAME, activity.MODE_PRIVATE);
        Auth.startOAuth2Authentication(activity, APP_KEY);
    }

    // TODO: at this point client must check whether the storage could be returned. If it is null, it must try again later
    public static DropboxStorage getDropboxStorage() {
        if (DropboxStorage == null) {
            DbxClientV2 client = getClient();
            if (client != null) {
                DropboxStorage = new DropboxStorage(client);
            }
        }

        return DropboxStorage;
    }

    private static DbxClientV2 getClient() {
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
                // TODO: App crashes
                Log.i("DropboxAccess", "Error creating dropbox client", e);
            }
        }

        // Client could not be create - probably not yet authenticated
        return null;
    }
}
