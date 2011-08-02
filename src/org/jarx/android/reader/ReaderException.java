package org.jarx.android.reader;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ReaderException extends Exception {

    public ReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReaderException(String message) {
        super(message);
    }

    public ReaderException(Throwable cause) {
        super(cause);
    }
}
