package com.infusion.jiramigrationtool;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;

public class JiraProjectMetadata {
    private final JiraConnector jiraConnector;
    // private String projectKey;

    private Iterable<Field> projectFields;
    private Iterable<IssuelinksType> projectIssueLinkTypes;
    private Iterable<Priority> projectPriorities;
    private Iterable<Resolution> projectResolutions;
    private Iterable<Status> projectStatuses;
    private Iterable<IssueType> projectIssueTypes;
    private Iterable<Version> projectVersions;
    private Iterable<BasicComponent> projectComponents;
    private ServerInfo projectServerInfo;

    public JiraProjectMetadata(final JiraConnector jiraConnector, final String projectKey) {
        this.jiraConnector = jiraConnector;
        // this.projectKey = projectKey;
    }

    public void generateMetadata() {
        this.setProjectFields(jiraConnector.getProjectFields());
        this.setProjectIssueLinkTypes(jiraConnector.getProjectIssueLinkTypes());
        this.setProjectPriorities(jiraConnector.getProjectPriorities());
        this.setProjectResolutions(jiraConnector.getProjectResolutions());
        this.setProjectStatuses(jiraConnector.getProjectStatuses());
        this.setProjectIssueTypes(jiraConnector.getProjectIssueTypes());
        this.setProjectVersions(jiraConnector.getProjectVersions());
        this.setProjectComponents(jiraConnector.getProjectComponents());
        this.setProjectServerInfo(jiraConnector.getProjectServerInfo());
    }

    public Iterable<Field> getProjectFields() {
        return projectFields;
    }

    public void setProjectFields(final Iterable<Field> projectFields) {
        this.projectFields = projectFields;
    }

    public Iterable<IssuelinksType> getProjectIssueLinkTypes() {
        return projectIssueLinkTypes;
    }

    public void setProjectIssueLinkTypes(final Iterable<IssuelinksType> projectIssueLinkTypes) {
        this.projectIssueLinkTypes = projectIssueLinkTypes;
    }

    public Iterable<Priority> getProjectPriorities() {
        return projectPriorities;
    }

    public void setProjectPriorities(final Iterable<Priority> projectPriorities) {
        this.projectPriorities = projectPriorities;
    }

    public Iterable<Resolution> getProjectResolutions() {
        return projectResolutions;
    }

    public void setProjectResolutions(final Iterable<Resolution> projectResolutions) {
        this.projectResolutions = projectResolutions;
    }

    public Iterable<Status> getProjectStatuses() {
        return projectStatuses;
    }

    public void setProjectStatuses(final Iterable<Status> projectStatuses) {
        this.projectStatuses = projectStatuses;
    }

    public Iterable<IssueType> getProjectIssueTypes() {
        return projectIssueTypes;
    }

    public void setProjectIssueTypes(final Iterable<IssueType> projectIssueTypes) {
        this.projectIssueTypes = projectIssueTypes;
    }

    public Iterable<Version> getProjectVersions() {
        return projectVersions;
    }

    public void setProjectVersions(final Iterable<Version> projectVersions) {
        this.projectVersions = projectVersions;
    }

    public Iterable<BasicComponent> getProjectComponents() {
        return projectComponents;
    }

    public void setProjectComponents(final Iterable<BasicComponent> projectComponents) {
        this.projectComponents = projectComponents;
    }

    public ServerInfo getProjectServerInfo() {
        return projectServerInfo;
    }

    public void setProjectServerInfo(final ServerInfo projectServerInfo) {
        this.projectServerInfo = projectServerInfo;
    }

}
