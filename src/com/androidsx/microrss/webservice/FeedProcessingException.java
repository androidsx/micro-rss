package com.androidsx.microrss.webservice;


/**
 * Exception to inform callers that a feed could not be properly loaded. Note
 * that it includes connection or parsing issues.
 * <p>
 * If the exception propagates until a user visible interface, the message may
 * be shown to the user, so make sure the message is appropriate.
 */
public final class FeedProcessingException extends Exception {
  private static final long serialVersionUID = 1L;
  private final UpdateTaskStatus status;

  public FeedProcessingException(String detailMessage, UpdateTaskStatus status) {
    super(detailMessage);
    this.status = status;
  }

  public FeedProcessingException(String detailMessage, Throwable throwable, UpdateTaskStatus status) {
    super(detailMessage, throwable);
    this.status = status;
  }

  public UpdateTaskStatus getStatus() {
    return status;
  }
  
}