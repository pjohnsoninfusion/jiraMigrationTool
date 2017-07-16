package com.infusion.jiramigrationtool.util;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZapiClientDataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ZapiClientDataGenerator.class.getName());

    public static String generateTestStepJsonString(final JSONObject jsonObj) throws JSONException {
        final String step = getPropertyFromJsonObject(jsonObj, "step");
        final String data = getPropertyFromJsonObject(jsonObj, "data");
        final String result = getPropertyFromJsonObject(jsonObj, "result");
        final StringBuilder builder = new StringBuilder();

        builder.append("{");
        builder.append("\"step\":\"").append(step).append("\",");
        builder.append("\"data\":\"").append(data).append("\",");
        builder.append("\"result\":\"").append(result).append("\"}");

        return builder.toString();
    }

    public static String generateExecutionJsonString(final JSONObject jsonObj, final Long destIssueId, final Long projectId, final String assignee)
            throws JSONException {

        final String cycleId = getPropertyFromJsonObject(jsonObj, "cycleId");
        final String versionId = getPropertyFromJsonObject(jsonObj, "versionId");
        final String assigneeType = getPropertyFromJsonObject(jsonObj, "assigneeType");

        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"cycleId\": \"").append(cycleId).append("\",");
        builder.append("\"issueId\": \"").append(destIssueId).append("\",");
        builder.append("\"projectId\": \"").append(projectId).append("\",");
        builder.append("\"versionId\": \"").append(versionId).append("\",");
        builder.append("\"assigneeType\": \"").append(assigneeType).append("\",");
        builder.append("\"assignee\": \"").append(assignee).append("\"}");

        return builder.toString();
    }

    public static String generateTestStepResultsJsonString(final JSONObject jsonObj, final Long testStepId) throws JSONException {
        final Integer stepId = getPropertyFromJsonObjectAsInteger(jsonObj, "stepId");
        final Integer executionId = getPropertyFromJsonObjectAsInteger(jsonObj, "executionId");
        final String status = getPropertyFromJsonObject(jsonObj, "status");
        final StringBuilder builder = new StringBuilder();

        builder.append("{");
        builder.append("\"stepId\":\"").append(stepId).append("\",");
        builder.append("\"issueId\":\"").append(testStepId).append("\",");
        builder.append("\"executionId\":\"").append(executionId).append("\",");
        builder.append("\"status\":\"").append(status).append("\"}");

        return builder.toString();
    }

    private static Integer getPropertyFromJsonObjectAsInteger(final JSONObject jsonObj, final String propName) {
        try {
            final Integer propValue = jsonObj.getInt(propName);
            return propValue;
        } catch (final Exception e) {
            logger.debug("Json property [{}] not found: {}", propName, e.getMessage(), e);
            return null;
        }
    }

    private static String getPropertyFromJsonObject(final JSONObject jsonObj, final String propName) throws JSONException {
        try {
            final String propValue = jsonObj.getString(propName);
            if (propValue == null) {
                return "";
            }
            return propValue;
        } catch (final Exception e) {
            logger.debug("Json property [{}] not found", propName);
            logger.trace("{}", propName, e.getMessage());
            return "";
        }
    }

}
