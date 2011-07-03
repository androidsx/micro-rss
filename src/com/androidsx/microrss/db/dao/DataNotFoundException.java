package com.androidsx.microrss.db.dao;

public class DataNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DataNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public DataNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
