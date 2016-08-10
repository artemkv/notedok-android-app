package com.notedok.notedok;

/**
 * Provides access to the instance of the master activity for the rest of the application.
 */
public final class MasterActivityProvider {
    // The storage that this provider provides
    private static MasterActivity MasterActivity;

    // Guards against using the class without initialization
    private static boolean Initialized = false;

    /**
     * Initializes the master activity provider.
     * @param activity The activity that serves as a master activity.
     */
    public static void initialize(MasterActivity activity) {
        if (activity == null)
            throw new IllegalArgumentException("activity");

        Initialized = true;

        MasterActivity = activity;
    }

    /**
     * Returns the instance of the master activity.
     * @return The instance of the master activity.
     */
    public static MasterActivity getMasterActivity() {
        if (!Initialized) {
            throw new IllegalStateException("The class is not initialized. Call initialize first.");
        }
        return MasterActivity;
    }
}
