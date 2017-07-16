package com.infusion.jiramigrationtool;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.NamedEntity;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;

public class JiraProjectMetadataMap {

    private final static Logger logger = LoggerFactory.getLogger(JiraProjectMetadataMap.class.getName());

    private final JiraProjectMetadata sourceMetadata;
    private final JiraProjectMetadata destMetadata;

    private final Map<String, Field> projectFieldsMap;
    private final Map<String, IssuelinksType> projectIssueLinkTypesMap;
    private final Map<String, Priority> projectPrioritiesMap;
    private final Map<String, Resolution> projectResolutionsMap;
    private final Map<String, Status> projectStatusesMap;
    private final Map<String, IssueType> projectIssueTypesMap;
    private final Map<String, Version> projectVersionsMap;
    private final Map<String, BasicComponent> projectComponentsMap;

    public JiraProjectMetadataMap(final JiraProjectMetadata sourceMetadata, final JiraProjectMetadata destMetadata) {
        this.sourceMetadata = sourceMetadata;
        this.destMetadata = destMetadata;

        projectFieldsMap = new HashMap<String, Field>();
        projectIssueLinkTypesMap = new HashMap<String, IssuelinksType>();
        projectPrioritiesMap = new HashMap<String, Priority>();
        projectResolutionsMap = new HashMap<String, Resolution>();
        projectStatusesMap = new HashMap<String, Status>();
        projectIssueTypesMap = new HashMap<String, IssueType>();
        projectVersionsMap = new HashMap<String, Version>();
        projectComponentsMap = new HashMap<String, BasicComponent>();
    }

    public void generate() throws IllegalStateException {
        generateMap(sourceMetadata.getProjectFields(), destMetadata.getProjectFields(), projectFieldsMap);
        generateMap(sourceMetadata.getProjectIssueLinkTypes(), destMetadata.getProjectIssueLinkTypes(), projectIssueLinkTypesMap);
        generateMap(sourceMetadata.getProjectPriorities(), destMetadata.getProjectPriorities(), projectPrioritiesMap);
        generateMap(sourceMetadata.getProjectResolutions(), destMetadata.getProjectResolutions(), projectResolutionsMap);
        generateMap(sourceMetadata.getProjectStatuses(), destMetadata.getProjectStatuses(), projectStatusesMap);
        generateMap(sourceMetadata.getProjectIssueTypes(), destMetadata.getProjectIssueTypes(), projectIssueTypesMap);
        generateMap(sourceMetadata.getProjectVersions(), destMetadata.getProjectVersions(), projectVersionsMap);
        generateMap(sourceMetadata.getProjectComponents(), destMetadata.getProjectComponents(), projectComponentsMap);
    }

    private void generateMap(final Iterable<? extends NamedEntity> sourceList, final Iterable<? extends NamedEntity> destList,
            final Map<String, ? extends NamedEntity> inputMap) throws IllegalStateException {
        for (final NamedEntity source : sourceList) {
            boolean exists = false;
            final String sourceName = source.getName();
            if (!inputMap.containsKey(sourceName)) {
                for (final NamedEntity dest : destList) {
                    if (dest.getName().equals(sourceName)) {
                        exists = true;
                        insertIntoMap(inputMap, dest, sourceName);
                    }
                }
                if (!exists) {
                    logger.warn("Mapping cannot be created for [{}]", sourceName);
                    // TODO: might need to insert a null mapping here
                }
            } else {
                logger.warn("Mapping already exists for [{}]", sourceName);
                // TODO: might need to do some sort of validation or remapping
            }
        }
    }

    private <T> void insertIntoMap(final Map<String, T> inputMap, final NamedEntity dest, final String sourceName) {
        if (dest instanceof Field) {
            inputMap.put(sourceName, (T) (Field) dest);
        } else if (dest instanceof IssuelinksType) {
            inputMap.put(sourceName, (T) (IssuelinksType) dest);
        } else if (dest instanceof Priority) {
            inputMap.put(sourceName, (T) (Priority) dest);
        } else if (dest instanceof Resolution) {
            inputMap.put(sourceName, (T) (Resolution) dest);
        } else if (dest instanceof Status) {
            inputMap.put(sourceName, (T) (Status) dest);
        } else if (dest instanceof IssueType) {
            inputMap.put(sourceName, (T) (IssueType) dest);
        } else if (dest instanceof Version) {
            inputMap.put(sourceName, (T) (Version) dest);
        } else if (dest instanceof BasicComponent) {
            inputMap.put(sourceName, (T) (BasicComponent) dest);
        }
    }

    public Map<String, Field> getProjectFieldsMap() {
        return projectFieldsMap;
    }

    public Map<String, IssuelinksType> getProjectIssueLinkTypesMap() {
        return projectIssueLinkTypesMap;
    }

    public Map<String, Priority> getProjectPrioritiesMap() {
        return projectPrioritiesMap;
    }

    public Map<String, Resolution> getProjectResolutionsMap() {
        return projectResolutionsMap;
    }

    public Map<String, Status> getProjectStatusesMap() {
        return projectStatusesMap;
    }

    public Map<String, IssueType> getProjectIssueTypesMap() {
        return projectIssueTypesMap;
    }

    public Map<String, Version> getProjectVersionsMap() {
        return projectVersionsMap;
    }

    public Map<String, BasicComponent> getProjectComponentsMap() {
        return projectComponentsMap;
    }

}
