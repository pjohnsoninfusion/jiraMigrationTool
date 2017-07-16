package com.infusion.jiramigrationtool;

import com.atlassian.jira.rest.client.api.NamedEntity;
import com.atlassian.jira.rest.client.api.domain.AssigneeType;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;

/**
 * updates dest jira metadata to support source
 * 
 * @author pjohnson
 *
 */
public class JiraProjectMetadataUpdater {

    private final JiraConnector jiraSourceConnector;
    private final JiraConnector jiraDestConnector;
    private final String destProjectKey;
    private final String sourceProjectKey;

    public JiraProjectMetadataUpdater(final JiraConnector jiraSourceConnector, final JiraConnector jiraDestConnector, final String destProjectKey,
            final String sourceProjectKey) {
        this.jiraSourceConnector = jiraSourceConnector;
        this.jiraDestConnector = jiraDestConnector;
        this.destProjectKey = destProjectKey;
        this.sourceProjectKey = sourceProjectKey;
    }

    /**
     * - Generates the metadata from two jira instances - Creates any new
     * metadata in dest that is required. - Returns a mapping of metadata from
     * source to dest
     * 
     * @param data.destMetadata
     * @param data.sourceMetadata
     * 
     */
    public JiraProjectMetadataMap prepareDestinationMetadata() {
        final JiraProjectMetadata sourceMetadata = new JiraProjectMetadata(jiraSourceConnector, sourceProjectKey);
        sourceMetadata.generateMetadata();

        generateAndCreateNewMetadata(sourceMetadata);

        final JiraProjectMetadata updatedDestMetadata = new JiraProjectMetadata(jiraDestConnector, destProjectKey);
        updatedDestMetadata.generateMetadata();
        final JiraProjectMetadataMap metadataMapping = generateMetadataMapping(sourceMetadata);
        return metadataMapping;
    }

    /**
     * Returns a mapping of metadata from source to dest
     * 
     * @param sourceMetadata
     * @return
     */
    private JiraProjectMetadataMap generateMetadataMapping(final JiraProjectMetadata sourceMetadata) {
        final JiraProjectMetadata destMetadata = new JiraProjectMetadata(jiraDestConnector, destProjectKey);
        destMetadata.generateMetadata();

        // assumption is that destination contains all metadata required for
        // mapping
        final JiraProjectMetadataMap mapping = new JiraProjectMetadataMap(sourceMetadata, destMetadata);
        mapping.generate();

        return mapping;
    }

    /**
     * Creates any new metadata in dest that is require
     * 
     * @param sourceMetadata
     * @param destMetadata
     */
    private void generateAndCreateNewMetadata(final JiraProjectMetadata sourceMetadata) {
        final JiraProjectMetadata destMetadata = new JiraProjectMetadata(jiraDestConnector, destProjectKey);
        destMetadata.generateMetadata();

        updateMetadata(sourceMetadata.getProjectFields(), destMetadata.getProjectFields());
        updateMetadata(sourceMetadata.getProjectIssueLinkTypes(), destMetadata.getProjectIssueLinkTypes());
        updateMetadata(sourceMetadata.getProjectPriorities(), destMetadata.getProjectPriorities());
        updateMetadata(sourceMetadata.getProjectResolutions(), destMetadata.getProjectResolutions());
        updateMetadata(sourceMetadata.getProjectStatuses(), destMetadata.getProjectStatuses());
        updateMetadata(sourceMetadata.getProjectIssueTypes(), destMetadata.getProjectIssueTypes());
        updateMetadata(sourceMetadata.getProjectVersions(), destMetadata.getProjectVersions());
        updateMetadata(sourceMetadata.getProjectComponents(), destMetadata.getProjectComponents());

    }

    private void updateMetadata(final Iterable<? extends NamedEntity> sourceList, final Iterable<? extends NamedEntity> destList) throws IllegalStateException {
        for (final NamedEntity source : sourceList) {
            boolean exists = false;
            final String sourceName = source.getName();
            for (final NamedEntity dest : destList) {
                if (dest.getName().equals(sourceName)) {
                    exists = true;
                }
            }
            if (!exists) {
                createNewData(source);
            }
        }
    }

    private <T> void createNewData(final NamedEntity source) {
        if (source instanceof Field) {
            final Field newField = JiraProjectMetadataGenerator.generateNewFieldFromSource((Field) source);
            jiraDestConnector.createField(newField);
        } else if (source instanceof IssuelinksType) {
            final IssuelinksType newIssuelinksType = JiraProjectMetadataGenerator.generateNewIssueLinkTypeFieldFromSource((IssuelinksType) source);
            jiraDestConnector.createIssuelinksType(newIssuelinksType);
        } else if (source instanceof Priority) {
            final Priority newPriority = JiraProjectMetadataGenerator.generateNewPriorityFromSource((Priority) source);
            jiraDestConnector.createPriority(newPriority);
        } else if (source instanceof Resolution) {
            final Resolution newResolution = JiraProjectMetadataGenerator.generateNewResolutionFromSource((Resolution) source);
            jiraDestConnector.createResolution(newResolution);
        } else if (source instanceof Status) {
            final Status newStatus = JiraProjectMetadataGenerator.generateNewStatusFromSource((Status) source);
            jiraDestConnector.createStatus(newStatus);
        } else if (source instanceof IssueType) {
            final IssueType newIssueType = JiraProjectMetadataGenerator.generateNewIssueTypeFromSource((IssueType) source);
            jiraDestConnector.createIssueType(newIssueType);
        } else if (source instanceof Version) {
            final Version newVersion = JiraProjectMetadataGenerator.generateNewVersionFromSource((Version) source);
            jiraDestConnector.createVersion(newVersion, destProjectKey);
        } else if (source instanceof BasicComponent) {
            final BasicComponent newBasicComponent = JiraProjectMetadataGenerator.generateNewBasicComponentFromSource((BasicComponent) source);
            final AssigneeType assigneeType = null;
            jiraDestConnector.createBasicComponent(newBasicComponent, null, assigneeType);
        }
    }

}
