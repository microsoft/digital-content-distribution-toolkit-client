package com.msr.bine_android.data.models;

public enum Category {
    ALL("All"),
    ENGLISH("English"),
    TAMIL("Tamil"),
    KANNADA("Kannada"),
    HINDI("Hindi"),
    TELUGU("Telugu"),
    BHOJPURI("Bhojpuri");

    private String stringValue;
    Category(String toString) {
        stringValue = toString;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
