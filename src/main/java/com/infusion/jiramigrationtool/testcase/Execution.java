package com.infusion.jiramigrationtool.testcase;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Execution extends ZephyrData {

    private static final String VERSION_UNSCHEDULED_ID = "-1";
    private static final String CYCLE_ADHOC_ID = "-1";

    private static final Logger logger = LoggerFactory.getLogger(Execution.class.getName());

    private Long id;
    private String executionStatus;
    private Long cycleId;
    private Long issueId;
    private Long projectId;
    private Long versionId;
    private String assigneeType;
    private String assignee;

    public Execution(final Long id, final String executionStatus, final Long cycleId, final Long versionId, final Long projectId, final String assignee,
            final Long issueId, final String assigneeType) {

        this.id = id;
        this.executionStatus = executionStatus;
        this.cycleId = cycleId;
        this.versionId = versionId;
        this.projectId = projectId;
        this.assignee = assignee;
        this.issueId = issueId;
        this.assigneeType = assigneeType;
    }

    public Execution(final JSONObject jsonObj) {
        try {
            id = getLongProperty("id", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            id = -1L;
        }
        try {
            executionStatus = getStringProperty("executionStatus", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            executionStatus = "";
        }
        try {
            cycleId = getLongProperty("cycleId", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            cycleId = -1L;
        }
        try {
            issueId = getLongProperty("issueId", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            issueId = -1L;
        }
        try {
            projectId = getLongProperty("projectId", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            projectId = -1L;
        }
        try {
            versionId = getLongProperty("versionId", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            versionId = -1L;
        }
        try {
            assigneeType = getStringProperty("assigneeType", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            assigneeType = "";
        }
        try {
            assignee = getStringProperty("assignedToUserName", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            assignee = "";
        }
    }

    public Execution(final Execution srcExecution, final Long destProjectId, final String destAssignee, final Long destIssueId) {
        issueId = destIssueId;
        projectId = destProjectId;
        assignee = destAssignee;
        cycleId = null;
        versionId = null;
        assigneeType = null;
    }

    @Override
    public String getPostJsonString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"cycleId\": \"").append(getCycleIdNotNull()).append("\",");
        builder.append("\"issueId\": \"").append(getIssueIdNotNull()).append("\",");
        builder.append("\"projectId\": \"").append(getProjectIdNotNull()).append("\",");
        builder.append("\"versionId\": \"").append(getVersionIdNotNull()).append("\",");
        builder.append("\"assigneeType\": \"").append(getAssigneeTypeNotNull()).append("\",");
        builder.append("\"assignee\": \"").append(getAssigneeNotNull()).append("\"}");

        return builder.toString();
    }

    private String getAssigneeNotNull() {
        if (assignee == null) {
            return "";
        }
        return assignee;
    }

    private String getAssigneeTypeNotNull() {
        if (assigneeType == null) {
            return "";
        }
        return assigneeType;
    }

    private String getVersionIdNotNull() {
        if (versionId == null) {
            return VERSION_UNSCHEDULED_ID;
        }
        return versionId.toString();
    }

    private String getProjectIdNotNull() {
        if (projectId == null) {
            return "";
        }
        return projectId.toString();
    }

    private String getIssueIdNotNull() {
        if (issueId == null) {
            return "";
        }
        return issueId.toString();
    }

    private String getCycleIdNotNull() {
        if (cycleId == null) {
            return CYCLE_ADHOC_ID;
        }
        return cycleId.toString();
    }

    @Override
    public String getUniqueKey() {
        final StringBuilder builder = new StringBuilder();
        builder.append("version->'").append(versionId).append("',");
        builder.append("cycle->'").append(cycleId).append("'");
        return builder.toString();
    }

    public String getStatusUpdateJsonString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{  \"status\": \"").append(executionStatus).append("\"}");
        return builder.toString();
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(final String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public Long getCycleId() {
        return cycleId;
    }

    public void setCycleId(final Long cycleId) {
        this.cycleId = cycleId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(final Long versionId) {
        this.versionId = versionId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(final Long projectId) {
        this.projectId = projectId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(final String assignee) {
        this.assignee = assignee;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(final Long issueId) {
        this.issueId = issueId;
    }

    public String getAssigneeType() {
        return assigneeType;
    }

    public void setAssigneeType(final String assigneeType) {
        this.assigneeType = assigneeType;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
