/**
 *
 */
package com.infusion.jiramigrationtool;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author trojek
 *
 */
public class MainInvoker {

    private static final String CUSTOM_APPENDER = "custom";
    private static final String STDOUT_APPENDER = "STDOUT";

    private String jiraUrlSource;
    private String jiraUsernameSource;
    private String jiraPasswordSource;
    private String jiraUrlDest;
    private String jiraUsernameDest;
    private String jiraPasswordDest;

    public void invoke() {
        final List<String> arguments = new ArrayList<>();

        for (final Field field : MainInvoker.class.getDeclaredFields()) {
            try {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    final Object value = field.get(this);
                    if (value != null) {
                        if (value instanceof String) {
                            arguments.add("-" + field.getName());
                            arguments.add(value.toString());
                        } else if ((value instanceof Boolean) && ((Boolean) value)) {
                            arguments.add("-" + field.getName());
                        }
                    }
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        final String[] args = arguments.toArray(new String[0]);
        try {
            Main.migrateJiraData(args);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLoggerName() {
        return MainInvoker.class.getName();
    }

    public MainInvoker jiraUrlSource(final String jiraUrl) {
        jiraUrlSource = jiraUrl;
        return this;
    }

    public MainInvoker jiraUsernameSource(final String jiraUsername) {
        jiraUsernameSource = jiraUsername;
        return this;
    }

    public MainInvoker jiraPasswordSource(final String jiraPassword) {
        jiraPasswordSource = jiraPassword;
        return this;
    }

    public MainInvoker jiraUrlDest(final String jiraUrl) {
        jiraUrlDest = jiraUrl;
        return this;
    }

    public MainInvoker jiraUsernameDest(final String jiraUsername) {
        jiraUsernameDest = jiraUsername;
        return this;
    }

    public MainInvoker jiraPasswordDest(final String jiraPassword) {
        jiraPasswordDest = jiraPassword;
        return this;
    }

}
