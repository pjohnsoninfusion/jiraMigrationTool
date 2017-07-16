package com.infusion.jiramigrationtool.testcase;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStep extends ZephyrData {
    private static final Logger logger = LoggerFactory.getLogger(TestStep.class.getName());

    private Long id;
    private Long issueId;
    private String step;
    private String data;
    private String result;

    public TestStep(final Long id, final Long issueId, final String step, final String data, final String result) {
        this.id = id;
        this.issueId = issueId;
        this.step = step;
        this.data = data;
        this.result = result;
    }

    public TestStep(final JSONObject jsonObj) {
        try {
            id = getLongProperty("id", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            id = -1L;
        }
        try {
            step = getStringProperty("step", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            step = "";
        }
        try {
            data = getStringProperty("data", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            data = "";
        }
        try {
            result = getStringProperty("result", jsonObj);
        } catch (final JSONException e) {
            logger.debug("{}", e.getMessage());
            result = "";
        }
    }

    @Override
    public String getUniqueKey() {
        final StringBuilder builder = new StringBuilder();
        builder.append("step->'").append(step).append("',");
        builder.append("data->'").append(data).append("',");
        builder.append("result->'").append(result).append("'");
        return builder.toString();
    }

    @Override
    public String getPostJsonString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("{");
        builder.append("\"step\":\"").append(step).append("\",");
        builder.append("\"data\":\"").append(data).append("\",");
        builder.append("\"result\":\"").append(result).append("\"}");

        return builder.toString();

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(final Long issueId) {
        this.issueId = issueId;
    }

    public String getStep() {
        return step;
    }

    public void setStep(final String step) {
        this.step = step;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }
}
