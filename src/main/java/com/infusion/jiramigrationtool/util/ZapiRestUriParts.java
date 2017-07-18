package com.infusion.jiramigrationtool.util;

public enum ZapiRestUriParts {
    GET_TEST_STEPS("/rest/zapi/latest/teststep/"),
    GET_EXECUTION_BY_ISSUE("/rest/zapi/latest/execution?issueId="),
    GET_EXECUTION_BY_ID("/rest/zapi/latest/execution/"),
    GET_TEST_STEP_RESULT_BY_EXECUTION_ID("/rest/zapi/latest/stepResult?executionId="),
    POST_TEST_STEPS("/rest/zapi/latest/teststep/"),
    POST_TEST_STEP_RESULT("/rest/zapi/latest/stepResult"),
    POST_EXECUTION_BY_ID("/rest/zapi/latest/execution"),
    PUT_STEP_RESULT_STATUS("/rest/zapi/latest/stepResult/"),
    PUT_EXECUTION_STATUS("/rest/zapi/latest/execution/??/execute");

    private String pathPart;

    ZapiRestUriParts(final String pathPart) {
        this.pathPart = pathPart;
    }

    public String pathPart() {
        return pathPart;
    }

}
