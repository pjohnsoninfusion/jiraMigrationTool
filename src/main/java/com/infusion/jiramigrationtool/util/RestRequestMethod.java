package com.infusion.jiramigrationtool.util;

public enum RestRequestMethod {
    POST("POST"),
    PUT("PUT");

    private String title;

    RestRequestMethod(final String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
