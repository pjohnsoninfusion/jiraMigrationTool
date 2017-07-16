package com.infusion.jiramigrationtool.testcase;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public abstract class ZephyrData {

    public abstract String getUniqueKey();

    public abstract String getPostJsonString();

    String getStringProperty(final String key, final JSONObject jsonData) throws JSONException {
        return jsonData.getString(key);
    }

    Long getLongProperty(final String key, final JSONObject jsonData) throws JSONException {
        return jsonData.getLong(key);
    }

}
