package com.infusion.jiramigrationtool;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.AssigneeType;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public interface JiraConnector {
    ImmutableSet<Issue> getIssuesIncludeParents(final ImmutableSet<String> issueIds);

    ImmutableMap<String, Issue> getIssuesByJql(final String jqlQuery);

    ImmutableMap<Long, Long> uploadIssues(final ImmutableSet<Issue> sourceIssues, final ImmutableMap<String, String> srcToDestMap,
            final JiraProjectMetadataMap srcToDestMetadataMap, final String foreignKeyFieldId, final JiraRestClient srcJiraRestClient);

    Iterable<Field> getProjectFields();

    Iterable<IssuelinksType> getProjectIssueLinkTypes();

    Iterable<Priority> getProjectPriorities();

    Iterable<Resolution> getProjectResolutions();

    Iterable<Status> getProjectStatuses();

    Iterable<IssueType> getProjectIssueTypes();

    ServerInfo getProjectServerInfo();

    Iterable<Version> getProjectVersions();

    Iterable<BasicComponent> getProjectComponents();

    void createField(final Field newField);

    void createIssuelinksType(final IssuelinksType newIssuelinksType);

    void createPriority(final Priority newPriority);

    void createResolution(final Resolution newResolution);

    void createStatus(final Status newStatus);

    void createIssueType(final IssueType newIssueType);

    void createBasicComponent(final BasicComponent newBasicComponent, final String leadUsername, final AssigneeType assigneeType);

    void createVersion(final Version newVersion, final String projectKey);

    ImmutableMap<String, Issue> getIssueBySummary(final String summary);

    Issue getIssueByKey(final String issueKey);

    void closeJiraRestClient(final JiraRestClient jiraRestClient);

    JiraRestClient createJiraRestClient();
}
