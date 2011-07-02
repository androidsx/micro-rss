package com.androidsx.microrss.configure;

import com.androidsx.microrss.webservice.FeedProcessingException;

/** The types of status results for the update task */
public enum UpdateTaskStatus {
    
    /**
     * Wimm: In the new model, the update service is totally decoupled from the activity execution.
     * We can't execute the method {@code forceUpdate} any more. That means, we won't really know
     * what's the status with the feed download and storage. Trust that there are any items :)
     */
    DONT_KNOW("Don't know"),
    
    /** Feed was read, parsed and stored. Awesome :) */
    OK("ok"),
    
    /**
     * A {@link FeedProcessingException} was caught. If you use this code,
     * you should customize the message, via {@link #setMsg}, so at least
     * we have a meaningful message for the user.
     */
    FEED_PROCESSING_EXCEPTION("Error while trying to load the feed"),
    
    /**
     * A {@link FeedProcessingException} was caught. If you use this code,
     * you should customize the message, via {@link #setMsg}, so at least
     * we have a meaningful message for the user.
     * <p>
     * This error does not require assistance from us, so the application
     * should not suggest to send us an email.
     */
    FEED_PROCESSING_EXCEPTION_NO_EMAIL("Error while trying to load the feed"),

    /**
     * Oops. We caught some other exception...
     * <p>
     * We don't know how to fix this error, so no email should be sent to us.
     */
    UNKNOWN_ERROR("Unknown error");

    private String msg;

    UpdateTaskStatus(String msg) {
      this.msg = msg;
    }

    public void setMsg(String msg) {
      this.msg = msg;
    }

    public String getMsg() {
      return msg;
    }

    @Override
    public String toString() {
      return msg;
    }
    
}