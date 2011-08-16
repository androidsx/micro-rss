package com.androidsx.microrss.view;

/**
 * Aware of the WIMM drag mechanism.
 */
interface Draggable {
    
    /**
     * Indicates whether drag-down should exit the activity.
     * 
     * @return true if and only if a drag-down move should trigger the exiting of the activity
     */
    boolean dragCanExit();
}
