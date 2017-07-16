package com.infusion.jiramigrationtool;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.AssigneeType;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.jira.rest.client.api.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.infusion.jiramigrationtool.util.JiraClientDataGenerator;
import com.infusion.jiramigrationtool.util.JiraRepoType;
import com.infusion.jiramigrationtool.util.MetadataType;

public class JiraConnectorImpl implements JiraConnector {
    private final static Logger logger = LoggerFactory.getLogger(JiraConnectorImpl.class.getName());
    private final Configuration configuration;
    private final JiraRepoType repoType;

    public JiraConnectorImpl(final Configuration configuration, final JiraRepoType repoType) {
        this.configuration = configuration;
        this.repoType = repoType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Field> getProjectFields() {
        return (Iterable<Field>) getProjectMetadata(MetadataType.FIELDS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<IssuelinksType> getProjectIssueLinkTypes() {
        return (Iterable<IssuelinksType>) getProjectMetadata(MetadataType.LINKS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Priority> getProjectPriorities() {
        return (Iterable<Priority>) getProjectMetadata(MetadataType.PRIORITIES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Resolution> getProjectResolutions() {
        return (Iterable<Resolution>) getProjectMetadata(MetadataType.RESOLUTIONS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Status> getProjectStatuses() {
        return (Iterable<Status>) getProjectMetadata(MetadataType.STATUSES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<IssueType> getProjectIssueTypes() {
        return (Iterable<IssueType>) getProjectMetadata(MetadataType.TYPES);
    }

    @Override
    public ServerInfo getProjectServerInfo() {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            final MetadataRestClient metadataClient = jiraRestClient.getMetadataClient();
            final ServerInfo serverInfo = metadataClient.getServerInfo().claim();
            return serverInfo;
        } catch (final RestClientException e) {
            final StringBuilder errorSb = new StringBuilder();
            for (final ErrorCollection error : e.getErrorCollections()) {
                for (final String errorMessage : error.getErrorMessages()) {
                    errorSb.append(errorMessage);
                }
            }
            logger.warn(e.getMessage());
            throw e;
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    private Iterable<?> getProjectMetadata(final MetadataType type) {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            final MetadataRestClient metadataClient = jiraRestClient.getMetadataClient();
            switch (type) {
                case FIELDS:
                    final Iterable<Field> fields = metadataClient.getFields().claim();
                    return fields;
                case LINKS:
                    final Iterable<IssuelinksType> links = metadataClient.getIssueLinkTypes().claim();
                    return links;
                case PRIORITIES:
                    final Iterable<Priority> priorities = metadataClient.getPriorities().claim();
                    return priorities;
                case RESOLUTIONS:
                    final Iterable<Resolution> resolutions = metadataClient.getResolutions().claim();
                    return resolutions;
                case STATUSES:
                    final Iterable<Status> statuses = metadataClient.getStatuses().claim();
                    return statuses;
                case TYPES:
                    final Iterable<IssueType> types = metadataClient.getIssueTypes().claim();
                    return types;
                default:
                    return null;
            }
        } catch (final RestClientException e) {
            final StringBuilder errorSb = new StringBuilder();
            for (final ErrorCollection error : e.getErrorCollections()) {
                for (final String errorMessage : error.getErrorMessages()) {
                    errorSb.append(errorMessage);
                }
            }
            logger.warn(e.getMessage());
            throw e;
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    @Override
    public Iterable<Version> getProjectVersions() {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            return jiraRestClient.getProjectClient().getProject(configuration.getProjectKey(repoType)).claim().getVersions();
        } catch (final RestClientException e) {
            final StringBuilder errorSb = new StringBuilder();
            for (final ErrorCollection error : e.getErrorCollections()) {
                for (final String errorMessage : error.getErrorMessages()) {
                    errorSb.append(errorMessage);
                }
            }
            logger.warn(e.getMessage());
            throw e;
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    @Override
    public Iterable<BasicComponent> getProjectComponents() {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            return jiraRestClient.getProjectClient().getProject(configuration.getProjectKey(repoType)).claim().getComponents();
        } catch (final RestClientException e) {
            final StringBuilder errorSb = new StringBuilder();
            for (final ErrorCollection error : e.getErrorCollections()) {
                for (final String errorMessage : error.getErrorMessages()) {
                    errorSb.append(errorMessage);
                }
            }
            logger.warn(e.getMessage());
            throw e;
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    @Override
    public void createField(final Field newField) {
        logger.debug("Not implemented");
    }

    @Override
    public void createIssuelinksType(final IssuelinksType newIssuelinksType) {
        logger.debug("Not implemented");
    }

    @Override
    public void createPriority(final Priority newPriority) {
        logger.debug("Not implemented");
    }

    @Override
    public void createResolution(final Resolution newResolution) {
        logger.debug("Not implemented");
    }

    @Override
    public void createStatus(final Status newStatus) {
        logger.debug("Not implemented");
    }

    @Override
    public void createIssueType(final IssueType newIssueType) {
        logger.debug("Not implemented");
    }

    @Override
    public void createVersion(final Version newVersion, final String projectKey) {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            createVersionInternal(jiraRestClient, newVersion, projectKey);
        } catch (final Exception e2) {
            logger.error("{}", e2.getMessage(), e2);
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    private void createVersionInternal(final JiraRestClient jiraRestClient, final Version newVersion, final String projectKey) {
        try {
            final VersionRestClient client = jiraRestClient.getVersionRestClient();
            client.createVersion(JiraClientDataGenerator.generateVersionInput(newVersion, projectKey)).claim();
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
        }
    }

    @Override
    public void createBasicComponent(final BasicComponent newBasicComponent, final String leadUsername, final AssigneeType assigneeType) {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            createBasicComponentInternal(jiraRestClient, newBasicComponent, leadUsername, assigneeType);
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    private void createBasicComponentInternal(final JiraRestClient jiraRestClient, final BasicComponent newBasicComponent, final String leadUsername,
            final AssigneeType assigneeType) {
        try {
            final ComponentRestClient client = jiraRestClient.getComponentClient();
            final ComponentInput componentInput = JiraClientDataGenerator.generateBasicComponentInput(newBasicComponent, leadUsername, assigneeType);
            final String projectKey = configuration.getProjectKey(JiraRepoType.DESTINATION);
            client.createComponent(projectKey, componentInput).claim();
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
        }
    }

    @Override
    public ImmutableMap<Long, Long> uploadIssues(final ImmutableSet<Issue> sourceIssues, final ImmutableMap<String, String> srcToDestMap,
            final JiraProjectMetadataMap srcToDestMetadataMap, final String foreignKeyFieldId, final JiraRestClient srcJiraRestClient) {

        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            logger.info("Uploading [{}] issues ", sourceIssues.size());
            return uploadIssuesToJiraClient(jiraRestClient, sourceIssues, srcToDestMap, srcToDestMetadataMap, foreignKeyFieldId, srcJiraRestClient);
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    private ImmutableMap<Long, Long> uploadIssuesToJiraClient(final JiraRestClient jiraRestClient, final ImmutableSet<Issue> sourceIssues,
            final ImmutableMap<String, String> srcToDestMap, final JiraProjectMetadataMap srcToDestMetadataMap, final String foreignKeyFieldId,
            final JiraRestClient srcJiraRestClient) {

        final IssueRestClient issueClient = jiraRestClient.getIssueClient();
        final Map<Long, Long> destToSrcIssueIdMap = new HashMap<Long, Long>();
        for (final Issue sourceIssue : sourceIssues) {

            try {
                final Map.Entry<String, IssueInput> sourceIssueInput = convertIssueToIssueInput(sourceIssue, srcToDestMetadataMap, foreignKeyFieldId);
                final IssueInput sourceInput = sourceIssueInput.getValue();
                final String sourceKey = sourceIssueInput.getKey();
                logger.info("source key = " + sourceKey);
                final String destKey = getDestIssueKeyFromSrcKey(srcToDestMap, sourceKey);
                String issueKey;
                Long destIssueId;
                final Long srcIssueId = sourceIssue.getId();
                if (destKey != null) {
                    issueClient.updateIssue(destKey, sourceInput).claim();
                    issueKey = destKey;
                    destIssueId = issueClient.getIssue(destKey).claim().getId();
                    logger.info("updated issue {}", issueKey);
                } else {
                    final BasicIssue newIssue = issueClient.createIssue(sourceInput).claim();
                    issueKey = newIssue.getKey();
                    destIssueId = newIssue.getId();
                    logger.info("created issue {} {}", issueKey, newIssue.getSelf());
                }
                destToSrcIssueIdMap.put(destIssueId, srcIssueId);
                final List<String> sprintNames = JiraClientDataGenerator.getSprintNames(sourceIssue);
                addComments(issueClient, jiraRestClient.getUserClient(), issueKey, sourceIssue.getComments(), sprintNames);
                addAttachments(issueClient, issueKey, sourceIssue.getAttachments(), srcJiraRestClient.getIssueClient());
                addWorklogs(issueClient, issueKey, sourceIssue.getWorklogs(), srcJiraRestClient.getIssueClient());
            } catch (final Exception e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
        return ImmutableMap.copyOf(destToSrcIssueIdMap);
    }

    private void addWorklogs(final IssueRestClient issueClient, final String issueKey, final Iterable<Worklog> worklogs, final IssueRestClient srcIssueClient) {

        final Issue destIssue = issueClient.getIssue(issueKey).claim();
        final Set<String> destIssueWorklogs = new HashSet<String>();
        for (final Worklog destWorklog : destIssue.getWorklogs()) {
            destIssueWorklogs.add(destWorklog.getComment());
        }
        if (worklogs != null) {
            for (final Worklog srcWorklog : worklogs) {
                if (!destIssueWorklogs.contains(JiraClientDataGenerator.generateWorklogCommentFromSource(srcWorklog))) {
                    final URI destWorklogUri = destIssue.getWorklogUri();
                    final WorklogInput worklogInput = JiraClientDataGenerator.generateWorklogFromSource(srcWorklog, destWorklogUri);
                    issueClient.addWorklog(destWorklogUri, worklogInput).claim();
                    logger.info("adding worklog [{}] to attachemnt URI [{}]", worklogInput, destWorklogUri);
                }
            }
        }
    }

    private Map.Entry<String, IssueInput> convertIssueToIssueInput(final Issue sourceIssue, final JiraProjectMetadataMap srcToDestMetadataMap,
            final String foreignKeyFieldId) {

        final String projectKey = configuration.getProjectKey(repoType);
        final Map.Entry<String, IssueInput> entry = new AbstractMap.SimpleEntry<String, IssueInput>(sourceIssue.getKey(), JiraClientDataGenerator
                .generateIssueInput(sourceIssue, projectKey, srcToDestMetadataMap, foreignKeyFieldId, configuration.getJiraFieldBlackListSet()));
        return entry;
    }

    private void addComments(final IssueRestClient issueClient, final UserRestClient userRestClient, final String issueKey, final Iterable<Comment> comments,
            final List<String> sprintNames) {
        final Issue destIssue = issueClient.getIssue(issueKey).claim();
        final Set<String> destIssueComments = new HashSet<String>();
        for (final Comment destComment : destIssue.getComments()) {
            destIssueComments.add(destComment.getBody());
        }
        final URI commentsUri = destIssue.getCommentsUri();
        for (final Comment srcComment : comments) {
            final String commentBody = "Original Author: '" + srcComment.getAuthor().getDisplayName() + "'\n" + srcComment.getBody();
            if (!destIssueComments.contains(srcComment.getBody()) && !destIssueComments.contains(commentBody)) {
                issueClient.addComment(commentsUri, Comment.valueOf(commentBody)).claim();
            }
        }
        final String sprintNamesCommentString = generateSprintComment(new ArrayList<String>());
        if ((sprintNamesCommentString != null) && !destIssueComments.contains(sprintNamesCommentString)) {
            issueClient.addComment(commentsUri, Comment.valueOf(sprintNamesCommentString)).claim();
        }
    }

    /**
     * Return null if sprintNames is empty or null
     * 
     * @param sprintNames
     * @return comment formatted comment string containing all source sprints
     */
    private String generateSprintComment(final List<String> srcSprintNames) {
        if ((srcSprintNames == null) || srcSprintNames.isEmpty()) {
            return null;
        }
        final StringBuilder builder = new StringBuilder("Sprint(s): ").append(srcSprintNames);
        return builder.toString();
    }

    private void addAttachments(final IssueRestClient issueClient, final String issueKey, final Iterable<Attachment> attachments,
            final IssueRestClient srcJiraRestClient) {

        final Issue destIssue = issueClient.getIssue(issueKey).claim();
        final Set<String> destIssueAttachmentFilenames = new HashSet<String>();
        for (final Attachment destAttachment : destIssue.getAttachments()) {
            destIssueAttachmentFilenames.add(destAttachment.getFilename());
        }
        if (attachments != null) {
            for (final Attachment srcAttachment : attachments) {
                if (!destIssueAttachmentFilenames.contains(srcAttachment.getFilename())) {
                    final URI attachmentsUri = destIssue.getAttachmentsUri();
                    final InputStream attachmentStream = getSourceAttachmentInputStream(srcJiraRestClient, srcAttachment);
                    issueClient.addAttachment(attachmentsUri, attachmentStream, srcAttachment.getFilename()).claim();
                    logger.info("adding attachemnt [{}] to attachemnt URI [{}]", srcAttachment.getFilename(), attachmentsUri);
                }
            }
        }
    }

    private InputStream getSourceAttachmentInputStream(final IssueRestClient srcJiraRestClient, final Attachment srcAttachment) {
        final URI srcAttachmentUri = srcAttachment.getContentUri();
        final InputStream attachmentStream = srcJiraRestClient.getAttachment(srcAttachmentUri).claim();
        return attachmentStream;
    }

    private String getDestIssueKeyFromSrcKey(final ImmutableMap<String, String> srcToDestMap, final String sourceKey) {
        return srcToDestMap.get(sourceKey);
    }

    @Override
    public Issue getIssueByKey(final String issueKey) {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            final IssueRestClient issueClient = jiraRestClient.getIssueClient();
            return issueClient.getIssue(issueKey).claim();
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    @Override
    public ImmutableMap<String, Issue> getIssuesByJql(final String jqlQuery) {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            return getIssuesByJqlInternal(jiraRestClient, jqlQuery);
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    @Override
    public ImmutableMap<String, Issue> getIssueBySummary(final String summary) {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            return getIssuesByJqlInternal(jiraRestClient, generateSummarySearchJQL(summary));
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    private String generateSummarySearchJQL(final String summary) {
        return "summary ~ \"" + summary + "\"";
    }

    private ImmutableMap<String, Issue> getIssuesByJqlInternal(final JiraRestClient jiraRestClient, final String jqlQuery) {
        logger.info("Getting issues for query: {}", jqlQuery);
        return convertIssueListToMap(retrieveIssuesFromJiraClient(jqlQuery, jiraRestClient));
    }

    private ImmutableMap<String, Issue> convertIssueListToMap(final List<Issue> issues) {
        return Maps.uniqueIndex(issues, new Function<Issue, String>() {
            @Override
            public String apply(final Issue issue) {
                return issue.getKey();
            }
        });
    }

    private List<Issue> retrieveIssuesFromJiraClient(final String searchQuery, final JiraRestClient jiraRestClient) {
        final SearchRestClient searchClient = jiraRestClient.getSearchClient();
        List<Issue> issues = new ArrayList<Issue>();
        if ((searchQuery != null) && !searchQuery.isEmpty()) {
            try {
                final SearchResult searchResult = searchClient.searchJql(searchQuery, configuration.getSearchLimitIssues(), 0, null).claim();
                issues = Lists.newArrayList(searchResult.getIssues());
                for (final Issue cur : issues) {
                    logger.info("[{}] - [{}] - [{}]", cur.getIssueType().getName(), cur.getSelf(), cur.getKey());
                }

                if (issues.size() < searchResult.getTotal()) {
                    logger.warn("Reached limit of max results {} - not all JIRA items returned - {} out of {}!!", searchResult.getMaxResults(), issues.size(),
                            searchResult.getTotal());
                }
                logger.info("Fetching issues from JIRA completed. {} issues fetched.", issues.size());
            } catch (final RestClientException e) {
                final StringBuilder errorSb = new StringBuilder();
                for (final ErrorCollection error : e.getErrorCollections()) {
                    for (final String errorMessage : error.getErrorMessages()) {
                        errorSb.append(errorMessage);
                    }
                }
                logger.warn(e.getMessage());
            }
        }
        return issues;
    }

    @Override
    public ImmutableSet<Issue> getIssuesIncludeParents(final ImmutableSet<String> issueIds) {
        JiraRestClient jiraRestClient = null;
        try {
            jiraRestClient = createJiraRestClient();
            return getIssuesIncludeParentsInternal(jiraRestClient, issueIds);
        } finally {
            closeJiraRestClient(jiraRestClient);
        }
    }

    @Override
    public void closeJiraRestClient(final JiraRestClient jiraRestClient) {
        try {
            if (jiraRestClient != null) {
                jiraRestClient.close();
            }
        } catch (final IOException e) {
            logger.warn("{}", e.getMessage(), e);
            throw new RuntimeException("Exception while contacting JIRA", e);
        }
    }

    @Override
    public JiraRestClient createJiraRestClient() {
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

        final AuthenticationHandler authenticationHandler = generateAuthenticationHandler();

        final String jiraUrl = configuration.getJiraUrl(repoType);
        return factory.create(URI.create(jiraUrl), authenticationHandler);
    }

    private AuthenticationHandler generateAuthenticationHandler() {
        final AuthenticationHandler authenticationHandler = new BasicHttpAuthenticationHandler(configuration.getJiraUsername(repoType),
                configuration.getJiraPassword(repoType));
        return authenticationHandler;
    }

    // TODO: update
    private ImmutableSet<Issue> getIssuesIncludeParentsInternal(final JiraRestClient jiraRestClient, final ImmutableSet<String> issueIds) {
        final SearchRestClient searchClient = jiraRestClient.getSearchClient();

        final String searchQuery = getSearchJQL(issueIds);

        logger.info("Getting issues for keys: {}", issueIds);

        final SearchResult searchResult = searchClient.searchJql(searchQuery, 500, 0, null).claim();
        final List<Issue> issues = Lists.newArrayList(searchResult.getIssues());

        if (issues.size() < searchResult.getTotal()) {
            logger.warn("Reached limit of max results {} - not all JIRA items returned - {} out of {}!!", searchResult.getMaxResults(), issues.size(),
                    searchResult.getTotal());
        }

        final ImmutableSet<String> parentKeysToFetch = FluentIterable.from(issues).filter(new Predicate<Issue>() {
            @Override
            public boolean apply(final Issue issue) {
                return issue.getIssueType().isSubtask();
            }
        }).transform(new Function<Issue, String>() {
            @Override
            public String apply(final Issue issue) {
                try {
                    return ((JSONObject) issue.getFieldByName("Parent").getValue()).get("key").toString();
                } catch (final JSONException e) {
                    throw new RuntimeException("JSON response from JIRA malformed - no parent key in subtask", e);
                }
            }
        })
                // Eliminate duplicates that have already been fetched
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(final String parentKey) {
                        return !issueIds.contains(parentKey);
                    }
                }).toSet();

        if (!parentKeysToFetch.isEmpty()) {
            logger.info("Fetching subtasks' parents which haven't been fetched already: {}", parentKeysToFetch);

            final String parentSearchQuery = getSearchJQL(parentKeysToFetch);
            final SearchResult parentSearchResult = searchClient.searchJql(parentSearchQuery).claim();
            Iterables.addAll(issues, parentSearchResult.getIssues());
        }

        logger.info("Fetching issues from JIRA completed. {} issues fetched.", issues.size());
        return ImmutableSet.copyOf(issues);
    }

    private static String getSearchJQL(final Iterable<String> issueIds) {
        return MessageFormat.format("key in ({0})", Joiner.on(",").join(issueIds));
    }

}
