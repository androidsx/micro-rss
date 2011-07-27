package com.androidsx.microrss.view;

/**
 * Reads, interprets and processes the data the holds the navigation-related data.
 * <p>
 * FIXME: What if we actually throw one of those IllegalArgumentExceptions? Force Close!
 */
class NavigationProcessor {
    private final int[] ids;
    private final int currentIndex;

    /**
     * Builds a new processor for the navigation data.
     * <p>
     * The items usually represent feeds or stories, that have been loaded from the DB or that come
     * from another activity through the extras
     * 
     * @param ids array of the IDs of the items
     * @param currentIndex current index for the current item
     */
    NavigationProcessor(int[] ids, int currentIndex) {
        this.ids = ids;
        this.currentIndex = currentIndex;
    }

    boolean isValidIndex() {
        return currentIndex >= 0 && currentIndex < ids.length;
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < ids.length;
    }

    int getCurrentIndex() {
        return currentIndex;
    }

    /** Only meaningful if {@code isValidIndex() == true}. */
    int getCurrentId() {
        return ids[currentIndex];
    }

    int getCount() {
        return ids.length;
    }

    boolean canGoLeft() {
        return isValidIndex(currentIndex - 1);
    }

    int goLeft() {
        if (canGoLeft()) {
            return currentIndex - 1;
        } else {
            throw new IllegalArgumentException("Can't go to the left of the item " + currentIndex);
        }
    }

    boolean canGoRight() {
        return isValidIndex(currentIndex + 1);
    }

    int goRight() {
        if (canGoRight()) {
            return currentIndex + 1;
        } else {
            throw new IllegalArgumentException("Can't go to the right of the item " + currentIndex);
        }
    }
}
