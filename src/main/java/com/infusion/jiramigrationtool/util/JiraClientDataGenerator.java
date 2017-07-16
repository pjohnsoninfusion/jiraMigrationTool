package com.infusion.jiramigrationtool.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.domain.AssigneeType;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;
import com.atlassian.jira.rest.client.api.domain.input.VersionInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInputBuilder;
import com.google.common.collect.ImmutableSet;
import com.infusion.jiramigrationtool.JiraProjectMetadataMap;

public class JiraClientDataGenerator {
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String SPRINT_FIELD_NAME = "Sprint";
    private static final Logger logger = LoggerFactory.getLogger(JiraClientDataGenerator.class.getName());

    public static VersionInput generateVersionInput(final Version newVersion, final String projectKey) {
        final VersionInputBuilder builder = new VersionInputBuilder(projectKey);
        builder.setArchived(newVersion.isArchived());
        builder.setDescription(newVersion.getDescription());
        builder.setName(newVersion.getName());
        builder.setReleased(newVersion.isReleased());
        builder.setReleaseDate(newVersion.getReleaseDate());
        return builder.build();
    }

    public static ComponentInput generateBasicComponentInput(final BasicComponent newBasicComponent, final String leadUsername,
            final AssigneeType assigneeType) {
        final String name = newBasicComponent.getName();
        final String description = newBasicComponent.getDescription();
        final ComponentInput newComponentInput = new ComponentInput(name, description, leadUsername, assigneeType);
        return newComponentInput;
    }

    public static String generateWorklogCommentFromSource(final Worklog destWorklog) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Author: ").append(destWorklog.getAuthor().getDisplayName()).append("\n");
        builder.append("Date Created: ").append(destWorklog.getCreationDate()).append("\n");
        builder.append("Date Started: ").append(destWorklog.getStartDate()).append("\n");
        builder.append("Update Author: ").append(destWorklog.getUpdateAuthor().getDisplayName()).append("\n");
        builder.append("Date Updated: ").append(destWorklog.getUpdateDate()).append("\n");
        return builder.toString();
    }

    public static IssueInput generateIssueInput(final Issue sourceIssue, final String projectKey, final JiraProjectMetadataMap srcToDestMetadataMap,
            final String foreignKeyFieldId, final ImmutableSet<String> fieldBlackList) {
        final IssueType type = srcToDestMetadataMap.getProjectIssueTypesMap().get(sourceIssue.getIssueType().getName());
        final IssueInputBuilder builder = new IssueInputBuilder();
        updateFieldValues(sourceIssue, srcToDestMetadataMap, fieldBlackList, builder);
        builder.setSummary(sourceIssue.getSummary());
        builder.setDescription(sourceIssue.getDescription());
        builder.setIssueType(type);
        builder.setProjectKey(projectKey);
        builder.setAffectedVersions(sourceIssue.getAffectedVersions());
        builder.setComponents(sourceIssue.getComponents());
        builder.setFieldValue(foreignKeyFieldId, sourceIssue.getKey());
        builder.setFixVersions(sourceIssue.getFixVersions());
        builder.setPriority(sourceIssue.getPriority());
        return builder.build();
    }

    public static List<String> getSprintNames(final Issue sourceIssue) {
        final List<String> srcSprintNameList = new ArrayList<String>();
        final IssueField srcSprintField = sourceIssue.getFieldByName(SPRINT_FIELD_NAME);
        final Object srcSprintFieldValue = srcSprintField.getValue();
        if (srcSprintFieldValue instanceof JSONArray) {
            List<ComplexIssueInputFieldValue> fieldList;
            try {
                fieldList = generateComplexFieldValues((JSONArray) srcSprintFieldValue);
                for (final ComplexIssueInputFieldValue fieldValue : fieldList) {
                    srcSprintNameList.add(fieldValue.getValuesMap().get(VALUE).toString());
                }
            } catch (final JSONException e) {
                logger.info("Sprint field from source is not in expected JSONArray format, unable to retrieve. {}", e.getMessage(), e);
            }
        } else {
            logger.info("Sprint field from source is not in expected JSONArray format, unable to retrieve");
        }
        return srcSprintNameList;
    }

    public static WorklogInput generateWorklogFromSource(final Worklog srcWorklog, final URI destWorklogUri) {
        final WorklogInputBuilder builder = new WorklogInputBuilder(destWorklogUri);
        builder.setComment(JiraClientDataGenerator.generateWorklogCommentFromSource(srcWorklog));
        builder.setMinutesSpent(srcWorklog.getMinutesSpent());
        builder.setStartDate(srcWorklog.getStartDate());
        final WorklogInput worklogInput = builder.build();
        return worklogInput;
    }

    private static List<String> updateFieldValues(final Issue sourceIssue, final JiraProjectMetadataMap srcToDestMetadataMap,
            final ImmutableSet<String> fieldBlackList, final IssueInputBuilder builder) {
        final Map<String, Field> srcNameToDesFieldMapping = srcToDestMetadataMap.getProjectFieldsMap();
        final List<String> srcSprintNameList = new ArrayList<String>();
        for (final IssueField srcField : sourceIssue.getFields()) {
            final String srcFieldName = srcField.getName();
            final Field desField = srcNameToDesFieldMapping.get(srcFieldName);
            if ((desField != null) && !fieldBlackList.contains(desField.getId())) {
                try {
                    final Object srcFieldValue = srcField.getValue();
                    if ((srcFieldValue instanceof String) || (srcFieldValue instanceof Integer)) {
                        logger.trace("desFieldID [{}] :: desFieldName [{}] :: value [{}]", desField.getId(), desField.getName(), srcFieldValue);
                        builder.setFieldValue(desField.getId(), srcFieldValue);
                    } else if (srcFieldValue instanceof JSONArray) {
                        List<ComplexIssueInputFieldValue> fieldList;
                        fieldList = generateComplexFieldValues((JSONArray) srcFieldValue);
                        if (!SPRINT_FIELD_NAME.equals(desField.getName())) {
                            builder.setFieldValue(desField.getId(), fieldList);
                            logger.trace("desFieldID [{}] :: desFieldName [{}] :: value [{}]", desField.getId(), desField.getName(), fieldList);
                        }
                    }
                } catch (final Exception e) {
                    logger.error("{}", e.getMessage(), e);
                }

            } else {
                logger.trace("No mapping exists for source field {}", srcFieldName);
            }
        }
        return srcSprintNameList;
    }

    private static List<ComplexIssueInputFieldValue> generateComplexFieldValues(final JSONArray srcFieldValue) throws JSONException {
        final List<String> valuesList = new ArrayList<String>();
        final List<ComplexIssueInputFieldValue> fieldList = new ArrayList<ComplexIssueInputFieldValue>();

        for (int i = 0; i < srcFieldValue.length(); i++) {
            try {
                final Object jsonArrayElement = srcFieldValue.get(i);
                logger.trace(jsonArrayElement.getClass().toString());
                if (jsonArrayElement instanceof String) {
                    final Map<String, String> nameValueMap = generateNameValueFromHashcodeString(jsonArrayElement);
                    if (nameValueMap.containsKey(NAME)) {
                        valuesList.add(nameValueMap.get(NAME));
                    }
                } else {
                    final JSONObject jsonobject = srcFieldValue.getJSONObject(i);
                    valuesList.add(jsonobject.getString(VALUE));
                }
            } catch (final Throwable e) {
                logger.error("{}", e.getMessage(), e);
            }
        }

        for (final String val : valuesList) {
            final Map<String, Object> mapValues = new HashMap<String, Object>();
            mapValues.put(VALUE, val);
            final ComplexIssueInputFieldValue fieldValue = new ComplexIssueInputFieldValue(mapValues);
            fieldList.add(fieldValue);
        }

        return fieldList;
    }

    private static Map<String, String> generateNameValueFromHashcodeString(final Object jsonArrayElement) {
        final String jsonArrayElementString = (String) jsonArrayElement;
        final String substring = jsonArrayElementString.substring(0, jsonArrayElementString.length() - 1);
        final String[] hashCodeParts = substring.split("\\[");
        final String[] stringParts = hashCodeParts[1].split(",");
        final Map<String, String> nameValueMap = new HashMap<String, String>();
        for (final String currStringPart : stringParts) {
            if (currStringPart.contains("=")) {
                final String[] nameAndValue = currStringPart.split("=");
                if (nameAndValue.length >= 2) {
                    nameValueMap.put(nameAndValue[0], nameAndValue[1]);
                }
            }
        }
        return nameValueMap;
    }

}
