package com.infusion.jiramigrationtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.infusion.jiramigrationtool.Configuration.Element;
import com.infusion.jiramigrationtool.util.JiraRepoType;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class.getName());

    public static void main(final String[] args) throws IOException {
        migrateJiraData(args);
        System.exit(0);
    }

    static void migrateJiraData(final String[] args) throws IOException {
        logger.info("Reading program parameters...");
        final ProgramParameters programParameters = new ProgramParameters();
        new JCommander(programParameters, args);

        final Configuration configuration = readConfiguration(programParameters);
        logger.info("Build configuration: {}", configuration);

        final JiraConnector jiraSourceConnector = new JiraConnectorImpl(configuration, JiraRepoType.SOURCE);
        final JiraConnector jiraDestConnector = new JiraConnectorImpl(configuration, JiraRepoType.DESTINATION);

        final JiraMigrator migrator = new JiraMigrator(jiraSourceConnector, jiraDestConnector, configuration);
        try {
            migrator.migrate();
            logger.info("Jira migration complete.");
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
        }

    }

    private static Configuration readConfiguration(final ProgramParameters programParameters) throws IOException {
        final String path = programParameters.configurationFilePath;
        final Properties properties = new Properties();

        if (StringUtils.isNotEmpty(path)) {
            logger.info("Using configuration file under {}", path);
            properties.load(new FileInputStream(new File(path)));
        } else {
            logger.info("Configuration file path parameter is note defined using only program parameters to biuld configuration");
        }

        return new Configuration(properties, programParameters);
    }

    public static class ProgramParameters {

        @Parameter(names = { "-configurationFilePath", "-conf" }, description = "Path to configuration file")
        private String configurationFilePath;

        @Element(Configuration.JIRA_URL_SOURCE)
        @Parameter(names = { "-jiraUrlSource" })
        private String jiraUrlSource;

        @Element(Configuration.JIRA_USERNAME_SOURCE)
        @Parameter(names = { "-jiraUsernameSource" })
        private String jiraUsernameSource;

        @Element(Configuration.JIRA_PASSWORD_SOURCE)
        @Parameter(names = { "-jiraPasswordSource" })
        private String jiraPasswordSource;

        @Element(Configuration.JIRA_URL_DEST)
        @Parameter(names = { "-jiraUrlDest" })
        private String jiraUrlDest;

        @Element(Configuration.JIRA_USERNAME_DEST)
        @Parameter(names = { "-jiraUsernameDest" })
        private String jiraUsernameDest;

        @Element(Configuration.JIRA_PASSWORD_DEST)
        @Parameter(names = { "-jiraPasswordDest" })
        private String jiraPasswordDest;

        @Element(Configuration.JIRA_ISSUEPATTERN)
        @Parameter(names = { "-jiraIssuePattern" })
        private String jiraIssuePattern;

        @Element(Configuration.COMPLETED_STATUSES)
        @Parameter(names = { "-completedStatuses" })
        private String completedStatuses;

        @Element(Configuration.FIX_VERSIONS)
        @Parameter(names = { "-jiraFixVersions" })
        private String fixVersions;

        @Element(Configuration.SOURCE_JQL_ISSUES)
        @Parameter(names = { "-sourceJqlIssues" })
        private String jqlIssues;

        @Element(Configuration.SOURCE_JQL_TESTCASES)
        @Parameter(names = { "-sourceJqlTestcases" })
        private String jqlTestcases;

        @Element(Configuration.SOURCE_SEARCH_LIMIT_ISSUES)
        @Parameter(names = { "-jiraSearchLimitIssues" })
        private String jiraSearchLimitIssues;

        @Element(Configuration.SOURCE_SEARCH_LIMIT_TESTCASES)
        @Parameter(names = { "-jiraSearchLimitTestcases" })
        private String jiraSearchLimitIssuesTestcases;

        @Element(Configuration.LABELS_TO_SKIP)
        @Parameter(names = { "-jiraLabelsToSkip" })
        private String labelsToSkip;

    }
}
