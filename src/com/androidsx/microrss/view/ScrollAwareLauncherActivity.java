package com.androidsx.microrss.view;

import com.wimm.framework.app.LauncherActivity;

/**
 * Launcher activity that is aware of the scroll mechanism.
 */
public class ScrollAwareLauncherActivity extends LauncherActivity {
    private Draggable draggable;

    public void setDragable(Draggable draggable) {
        this.draggable = draggable;
    }

    @Override
    public boolean dragCanExit() {
        return draggable.dragCanExit();
    }
}
