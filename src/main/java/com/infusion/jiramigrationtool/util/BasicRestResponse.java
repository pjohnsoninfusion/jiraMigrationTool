package com.infusion.jiramigrationtool.util;

public class BasicRestResponse {

    private final Integer statusCode;
    private final String response;

    public BasicRestResponse(final Integer statusCode, final String response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }
}
