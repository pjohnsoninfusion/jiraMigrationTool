package com.infusion.jiramigrationtool;

import java.net.URI;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;

public class JiraProjectMetadataGenerator {

    public static BasicComponent generateNewBasicComponentFromSource(final BasicComponent source) {
        final URI self = source.getSelf();
        final Long id = source.getId();
        final String name = source.getName();
        final String description = source.getDescription();
        return new BasicComponent(self, id, name, description);
    }

    public static Version generateNewVersionFromSource(final Version source) {
        final URI self = source.getSelf();
        final Long id = source.getId();
        final String name = source.getName();
        final String description = source.getDescription();
        final boolean archived = source.isArchived();
        final boolean released = source.isReleased();
        final DateTime releaseDate = source.getReleaseDate();
        return new Version(self, id, name, description, archived, released, releaseDate);
    }

    public static IssueType generateNewIssueTypeFromSource(final IssueType source) {
        final URI self = source.getSelf();
        final Long id = source.getId();
        final String name = source.getName();
        final boolean isSubtask = source.isSubtask();
        final String description = source.getDescription();
        final URI iconUri = source.getIconUri();
        return new IssueType(self, id, name, isSubtask, description, iconUri);
    }

    public static Status generateNewStatusFromSource(final Status source) {
        final URI self = source.getSelf();
        final Long id = source.getId();
        final String name = source.getName();
        final String description = source.getDescription();
        final URI iconUrl = source.getIconUrl();
        return new Status(self, id, name, description, iconUrl);
    }

    public static Resolution generateNewResolutionFromSource(final Resolution source) {
        final URI self = source.getSelf();
        final Long id = source.getId();
        final String name = source.getName();
        final String description = source.getDescription();
        return new Resolution(self, id, name, description);
    }

    public static Priority generateNewPriorityFromSource(final Priority source) {
        final URI self = source.getSelf();
        final Long id = source.getId();
        final String name = source.getName();
        final String statusColor = source.getStatusColor();
        final String description = source.getDescription();
        final URI iconUri = source.getIconUri();
        return new Priority(self, id, name, statusColor, description, iconUri);
    }

    public static IssuelinksType generateNewIssueLinkTypeFieldFromSource(final IssuelinksType source) {
        final URI self = source.getSelf();
        final String id = source.getId();
        final String name = source.getName();
        final String inward = source.getInward();
        final String outward = source.getOutward();
        return new IssuelinksType(self, id, name, inward, outward);
    }

    public static Field generateNewFieldFromSource(final Field source) {
        final String id = source.getId();
        final String name = source.getName();
        final FieldType fieldType = source.getFieldType();
        final boolean orderable = source.isOrderable();
        final boolean navigable = source.isNavigable();
        final boolean searchable = source.isSearchable();
        final FieldSchema schema = source.getSchema();
        return new Field(id, name, fieldType, orderable, navigable, searchable, schema);
    }

}