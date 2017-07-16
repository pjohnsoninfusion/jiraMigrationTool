package com.infusion.jiramigrationtool.testcase;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepResult extends ZephyrData {
    private static final Logger logger = LoggerFactory.getLogger(StepResult.class.getName());

    private Long id;
    private Long result;
    private String status;
    private Long issueId;
    private Long executionId;
    private Long stepId;

    public StepResult(final Long id, final Long result, final String status, final Long issueId, final Long executionId, final Long stepId) {
        this.id = id;
        this.status = status;
        this.executionId = executionId;
        this.stepId = stepId;
        this.result = result;
        this.status = status;
        this.issueId = issueId;
    }

    public StepResult(final Long stepId, final Long issueId, final Long executionId, final String status) {
        this.executionId = executionId;
        this.stepId = stepId;
        this.status = status;
        this.issueId = issueId;
    }

    public StepResult(final JSONObject stepResultJSONObject, final Long issueId) {
        try {
            id = getLongProperty("id", stepResultJSONObject);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            id = -1L;
        }
        try {
            status = getStringProperty("status", stepResultJSONObject);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            status = "";
        }
        try {
            executionId = getLongProperty("executionId", stepResultJSONObject);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            executionId = -1L;
        }
        try {
            stepId = getLongProperty("stepId", stepResultJSONObject);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            stepId = -1L;
        }
        this.issueId = issueId;
    }

    @Override
    public String getUniqueKey() {
        final StringBuilder builder = new StringBuilder();
        builder.append("stepId->'").append(stepId).append("',");
        builder.append("issueId->'").append(issueId).append("',");
        builder.append("executionId->'").append(executionId).append("',");
        builder.append("status->'").append(status).append("'");
        return builder.toString();
    }

    @Override
    public String getPostJsonString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("{");
        builder.append("\"stepId\":\"").append(stepId).append("\",");
        builder.append("\"issueId\":\"").append(issueId).append("\",");
        builder.append("\"executionId\":\"").append(executionId).append("\",");
        builder.append("\"status\":\"").append(status).append("\"}");

        return builder.toString();
    }

    public String getStatusUpdateJsonString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"status\":\"").append(status).append("\"}");
        return builder.toString();
    }

    public Long getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setResult(final Long result) {
        this.result = result;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setIssueId(final Long issueId) {
        this.issueId = issueId;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setExecutionId(final Long executionId) {
        this.executionId = executionId;
    }

    public void setStepId(final Long stepId) {
        this.stepId = stepId;
    }

}
