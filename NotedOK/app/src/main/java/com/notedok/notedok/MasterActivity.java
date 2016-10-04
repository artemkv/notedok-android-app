package com.notedok.notedok;

/**
 * The activity that represents the master part in the master-detail setup.
 */
public interface MasterActivity {
    public static final String FILES_INTENT_EXTRA_NAME = "files";
    public static final String POSITION_INTENT_EXTRA_NAME = "pos";

    /**
     * Transfers control to the details activity.
     * @param fileList The file list for notes visible in the master view.
     * @param position The position of the visible note in the master view.
     */
    void switchToDetailActivity(FileList fileList, int position);
}
