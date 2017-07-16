package com.infusion.jiramigrationtool.util;

public class ZapiRestClient extends BasicRestClient {

    private static final String SUBSTITUTION_INDICATOR = "??";
    private final String urlRoot;

    public ZapiRestClient(final String username, final String password, final String urlRoot) {
        super(username, password);
        if (urlRoot.endsWith("\\") || urlRoot.endsWith("/")) {
            this.urlRoot = urlRoot.substring(0, urlRoot.length() - 1);
        } else {
            this.urlRoot = urlRoot;
        }
    }

    // GETS
    public BasicRestResponse getIssueTestSteps(final Long issueId) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.GET_TEST_STEPS.pathPart()).append(issueId);
        final String urlString = builder.toString();
        return super.get(urlString);
    }

    public BasicRestResponse getIssueTestStepResultByExecutionId(final Long executionId) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.GET_TEST_STEP_RESULT_BY_EXECUTION_ID.pathPart()).append(executionId);
        final String urlString = builder.toString();
        return super.get(urlString);
    }

    public BasicRestResponse getExecutionsByIssueId(final Long issueId) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.GET_EXECUTION_BY_ISSUE.pathPart()).append(issueId);
        final String urlString = builder.toString();
        return super.get(urlString);
    }

    public BasicRestResponse getExecutionsByExecutionId(final Long execId) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.GET_EXECUTION_BY_ID.pathPart()).append(execId);
        final String urlString = builder.toString();
        return super.get(urlString);
    }

    // POSTS
    public BasicRestResponse createIssueTestSteps(final Long issueId, final String jsonString) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.POST_TEST_STEPS.pathPart()).append(issueId);
        final String urlString = builder.toString();
        return super.postPut(urlString, jsonString, RestRequestMethod.POST);
    }

    public BasicRestResponse createExecution(final String jsonString) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.POST_EXECUTION_BY_ID.pathPart());
        final String urlString = builder.toString();
        return super.postPut(urlString, jsonString, RestRequestMethod.POST);
    }

    public BasicRestResponse createIssueTestStepResult(final String jsonString) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.POST_TEST_STEP_RESULT.pathPart());
        final String urlString = builder.toString();
        return super.postPut(urlString, jsonString, RestRequestMethod.POST);
    }

    // PUTS
    public BasicRestResponse updateExecution(final String jsonString, final String executionId) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        String pathPart = ZapiRestUriParts.PUT_EXECUTION_STATUS.pathPart();
        if (pathPart.contains(SUBSTITUTION_INDICATOR)) {
            pathPart = pathPart.replace(SUBSTITUTION_INDICATOR, executionId);
        }
        builder.append(pathPart);
        final String urlString = builder.toString();
        return super.postPut(urlString, jsonString, RestRequestMethod.PUT);
    }

    public BasicRestResponse updateStepResult(final String jsonString, final String stepResultId) {
        final StringBuilder builder = new StringBuilder(urlRoot);
        builder.append(ZapiRestUriParts.PUT_STEP_RESULT_STATUS.pathPart()).append(stepResultId);
        final String urlString = builder.toString();
        return super.postPut(urlString, jsonString, RestRequestMethod.PUT);
    }

}
