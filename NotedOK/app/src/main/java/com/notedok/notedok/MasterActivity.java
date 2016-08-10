package com.notedok.notedok;

/**
 * The activity that represents the master part in the master-detail setup.
 */
public interface MasterActivity {
    /**
     * Transfers control to the details activity.
     * @param position The position of the visible note in the master view.
     */
    void switchToDetailActivity(int position);
}
