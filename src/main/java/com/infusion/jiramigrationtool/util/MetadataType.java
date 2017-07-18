package com.infusion.jiramigrationtool.util;

public enum MetadataType {
    FIELDS("Fields"),
    LINKS("Links"),
    TYPES("Types"),
    PRIORITIES("Priorities"),
    RESOLUTIONS("Resolutions"),
    STATUSES("Statuses");

    private String title;

    MetadataType(final String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

}
