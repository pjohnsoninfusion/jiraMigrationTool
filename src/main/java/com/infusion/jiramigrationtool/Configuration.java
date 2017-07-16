package com.infusion.jiramigrationtool;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.infusion.jiramigrationtool.util.CollectionUtils;
import com.infusion.jiramigrationtool.util.JiraRepoType;

public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class.getName());

    static final String JIRA_URL_SOURCE = "jira.url.source";
    static final String JIRA_USERNAME_SOURCE = "jira.username.source";
    static final String JIRA_PASSWORD_SOURCE = "jira.password.source";

    static final String JIRA_URL_DEST = "jira.url.dest";
    static final String JIRA_USERNAME_DEST = "jira.username.dest";
    static final String JIRA_PASSWORD_DEST = "jira.password.dest";
    static final String JIRA_PROJECT_KEY_DEST = "jira.projectKey.dest";
    static final String JIRA_PROJECT_KEY_SOURCE = "jira.projectKey.source";

    static final String JIRA_SRC_REF_KEY = "jira.srcRefKey";
    static final String JIRA_FIELD_BLACKLIST = "jira.field.blackList";

    static final String SOURCE_JQL_ISSUES = "jira.source.jql.issues";
    static final String SOURCE_JQL_TESTCASES = "jira.source.jql.testcases";
    static final String SOURCE_SEARCH_LIMIT_ISSUES = "jira.searchLimit.issues";
    static final String SOURCE_SEARCH_LIMIT_TESTCASES = "jira.searchLimit.testcases";

    static final String JIRA_ISSUEPATTERN = "jira.issuepattern";
    static final String COMPLETED_STATUSES = "jira.completedStatuses";
    static final String FIX_VERSIONS = "jira.fixVersions";
    static final String LABELS_TO_SKIP = "jira.labelsToSkip";

    private final Properties properties;

    public Configuration(final Properties properties, final JiraRepoType type) {
        this.properties = properties;
    }

    public Configuration(final Properties properties, final Object configurationContainer) {
        this.properties = properties;

        for (final Field field : configurationContainer.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Element.class)) {
                try {
                    final String key = field.getAnnotation(Element.class).value();
                    field.setAccessible(true);
                    final String value = (String) field.get(configurationContainer);
                    if (isNotEmpty(value)) {
                        properties.put(key, value);
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String getJiraUrl(final JiraRepoType type) {
        switch (type) {
            case SOURCE:
                return getJiraUrlSource();
            case DESTINATION:
                return getJiraUrlDest();
            default:
                throw new IllegalStateException("Jira type unknown");
        }
    }

    public String getJiraPassword(final JiraRepoType type) {
        switch (type) {
            case SOURCE:
                return getJiraPasswordSource();
            case DESTINATION:
                return getJiraPasswordDest();
            default:
                throw new IllegalStateException("Jira type unknown");
        }
    }

    private String getJiraPasswordSource() {
        return properties.getProperty(JIRA_PASSWORD_SOURCE);
    }

    private String getJiraPasswordDest() {
        return properties.getProperty(JIRA_PASSWORD_DEST);
    }

    private String getJiraUrlSource() {
        return properties.getProperty(JIRA_URL_SOURCE);
    }

    private String getJiraUrlDest() {
        return properties.getProperty(JIRA_URL_DEST);
    }

    public String getJiraUsername(final JiraRepoType type) {
        switch (type) {
            case SOURCE:
                return getJiraUsernameSource();
            case DESTINATION:
                return getJiraUsernameDest();
            default:
                throw new IllegalStateException("Jira type unknown");
        }
    }

    private String getJiraUsernameSource() {
        return properties.getProperty(JIRA_USERNAME_SOURCE);
    }

    private String getJiraUsernameDest() {
        return properties.getProperty(JIRA_USERNAME_DEST);
    }

    public String getJiraSrcRefKey() {
        return properties.getProperty(JIRA_SRC_REF_KEY);
    }

    public String[] getJiraFieldBlackList() {
        return CollectionUtils.stringToArray(",", properties.getProperty(JIRA_FIELD_BLACKLIST));
    }

    public ImmutableSet<String> getJiraFieldBlackListSet() {
        return CollectionUtils.arrayToImmutableSet(getJiraFieldBlackList());
    }

    public String getProjectKey(final JiraRepoType type) {
        switch (type) {
            case SOURCE:
                return getSourceProjectKey();
            case DESTINATION:
                return getDestinationProjectKey();
            default:
                throw new IllegalStateException("Jira type unknown");
        }

    }

    private String getDestinationProjectKey() {
        return properties.getProperty(JIRA_PROJECT_KEY_DEST);
    }

    private String getSourceProjectKey() {
        return properties.getProperty(JIRA_PROJECT_KEY_SOURCE);
    }

    public String getJiraIssuePattern() {
        return properties.getProperty(JIRA_ISSUEPATTERN);
    }

    public Integer getSearchLimitIssues() {
        return Integer.parseInt(properties.getProperty(SOURCE_SEARCH_LIMIT_ISSUES));
    }

    public Integer getSearchLimitTestcases() {
        return Integer.parseInt(properties.getProperty(SOURCE_SEARCH_LIMIT_TESTCASES));
    }

    public String[] getCompletedStatuses() {
        return CollectionUtils.stringToArray(",", properties.getProperty(COMPLETED_STATUSES));
    }

    public String[] getFixVersions() {
        return CollectionUtils.stringToArray(",", properties.getProperty(FIX_VERSIONS));
    }

    public ImmutableSet<String> getFixVersionsSet() {
        return CollectionUtils.arrayToImmutableSet(getFixVersions());
    }

    public String getSourceJqlIssues() {
        return properties.getProperty(SOURCE_JQL_ISSUES);
    }

    public String getSourceJqlTestcases() {
        return properties.getProperty(SOURCE_JQL_TESTCASES);
    }

    public String[] getLabelsToSkip() {
        return CollectionUtils.stringToArray(",", properties.getProperty(LABELS_TO_SKIP));
    }

    public ImmutableSet<String> getLabelsToSkipSet() {
        return CollectionUtils.arrayToImmutableSet(getLabelsToSkip());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Configuration[");
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            if (!entry.getKey().toString().contains("password")) {
                if (builder.length() > 14) {
                    builder.append("|");
                }
                builder.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        builder.append("]");
        return builder.toString();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Element {
        public String value();
    }
}
