package com.microsoft.mobile.polymer.mishtu.storage.snappyDB;

public class NoSqlDBException extends Exception {
    public NoSqlDBException() {
    }

    public NoSqlDBException(String detailMessage) {
        super(detailMessage);
    }

    public NoSqlDBException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoSqlDBException(Throwable throwable) {
        super(throwable);
    }
}
