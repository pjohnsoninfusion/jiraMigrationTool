package com.infusion.jiramigrationtool.util;

public enum JiraRepoType {
    SOURCE("Source"),
    DESTINATION("Destination");

    private String title;

    JiraRepoType(final String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

}
