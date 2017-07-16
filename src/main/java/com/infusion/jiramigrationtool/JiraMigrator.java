package com.infusion.jiramigrationtool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infusion.jiramigrationtool.testcase.Execution;
import com.infusion.jiramigrationtool.testcase.StepResult;
import com.infusion.jiramigrationtool.testcase.TestStep;
import com.infusion.jiramigrationtool.testcase.ZephyrData;
import com.infusion.jiramigrationtool.util.BasicRestResponse;
import com.infusion.jiramigrationtool.util.JiraRepoType;
import com.infusion.jiramigrationtool.util.ZapiRestClient;

public class JiraMigrator {

    private static final int HTTP_OK = 200;

    private final static Logger logger = LoggerFactory.getLogger(JiraMigrator.class.getName());

    private final JiraConnector jiraSourceConnector;
    private final JiraConnector jiraDestConnector;
    private final Configuration configuration;
    private final JiraProjectMetadataUpdater metadataUpdater;

    public JiraMigrator(final JiraConnector jiraSourceConnector, final JiraConnector jiraDestConnector, final Configuration configuration) {
        this.jiraSourceConnector = jiraSourceConnector;
        this.jiraDestConnector = jiraDestConnector;
        this.configuration = configuration;
        final String destProjectKey = configuration.getProjectKey(JiraRepoType.DESTINATION);
        final String sourceProjectKey = configuration.getProjectKey(JiraRepoType.SOURCE);
        metadataUpdater = new JiraProjectMetadataUpdater(this.jiraSourceConnector, this.jiraDestConnector, destProjectKey, sourceProjectKey);
    }

    public void migrate() {
        try {
            final JiraProjectMetadataMap srcToDestMetadataMap = metadataUpdater.prepareDestinationMetadata();

            final ImmutableSet<Issue> sourceIssueSet = getSourceIssues();
            migrateIssues(srcToDestMetadataMap, sourceIssueSet);

            final ImmutableSet<Issue> sourceTestcaseSet = getSourceTestCases();
            migrateTestcases(srcToDestMetadataMap, sourceTestcaseSet);

            logger.info("Jira migration complete.");
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
        }
    }

    /**
     * copies jira issues from one instance to another update: search for a dest defect with the FOR ID set to the issue ID
     * 
     * @param srcToDestMetadataMap
     * @param sourceIssueSet
     */
    private void migrateIssues(final JiraProjectMetadataMap srcToDestMetadataMap, final ImmutableSet<Issue> sourceIssueSet) {
        final ImmutableMap<String, String> srcToDestMap = generateMapOfImportedIssuesByDestSrcKeys();
        final String foreignKeyFieldId = getForeignKeyFieldId();

        jiraDestConnector.uploadIssues(sourceIssueSet, srcToDestMap, srcToDestMetadataMap, foreignKeyFieldId, jiraSourceConnector.createJiraRestClient());
    }

    /**
     * copies jira testcases from one instance to another update: search for a dest defect with the FOR ID set to the issue ID
     * 
     * @param srcToDestMetadataMap
     * @param sourceIssueSet
     */
    private void migrateTestcases(final JiraProjectMetadataMap srcToDestMetadataMap, final ImmutableSet<Issue> sourceIssueSet) {
        final ImmutableMap<Long, Long> destToSrcIssueIdMap = uploadDestIssues(srcToDestMetadataMap, sourceIssueSet);

        updateZephyrData(destToSrcIssueIdMap);

    }

    private void updateZephyrData(final ImmutableMap<Long, Long> destToSrcIssueIdMap) {
        final ZapiRestClient srcClient = new ZapiRestClient(configuration.getJiraUsername(JiraRepoType.SOURCE),
                configuration.getJiraPassword(JiraRepoType.SOURCE), configuration.getJiraUrl(JiraRepoType.SOURCE));

        final ZapiRestClient destClient = new ZapiRestClient(configuration.getJiraUsername(JiraRepoType.DESTINATION),
                configuration.getJiraPassword(JiraRepoType.DESTINATION), configuration.getJiraUrl(JiraRepoType.DESTINATION));

        try {
            // executions must be created before test results can be updated
            for (final Map.Entry<Long, Long> currentMapping : destToSrcIssueIdMap.entrySet()) {
                final Long destIssueId = currentMapping.getKey();
                final Long srcIssueId = currentMapping.getValue();

                final ImmutableMap<Long, TestStep> srcTestSteps = getTestSteps(srcClient, srcIssueId);
                final ImmutableMap<Long, Execution> srcExecutions = getExecutions(srcClient, srcIssueId);
                final ImmutableMap<Long, StepResult> srcStepResults = getStepResults(srcClient, srcIssueId, srcExecutions.values());
                final ImmutableMap<Long, TestStep> destTestSteps = getTestSteps(destClient, destIssueId);
                final ImmutableMap<Long, Execution> destExecutions = getExecutions(destClient, destIssueId);
                final ImmutableMap<Long, StepResult> destStepResults = getStepResults(destClient, destIssueId, destExecutions.values());

                updateTestSteps(destTestSteps.values(), srcTestSteps.values(), destClient, destIssueId);
                updateExecutions(destExecutions.values(), srcExecutions.values(), destClient, destIssueId);
                updateTestStepResults(destStepResults.values(), destTestSteps.values(), srcStepResults.values(), srcTestSteps.values(), destClient,
                        destIssueId);
            }
        } catch (final JSONException e) {
            logger.warn("Error updating Zephyr data: {}", e.getMessage());
            logger.trace("{}", e.getMessage(), e);
        }
    }

    private void updateTestStepResults(final ImmutableCollection<StepResult> destStepResults, final ImmutableCollection<TestStep> destTestSteps,
            final ImmutableCollection<StepResult> srcStepResults, final ImmutableCollection<TestStep> srcTestSteps, final ZapiRestClient destClient,
            final Long destIssueId) {

        for (final StepResult srcSR : srcStepResults) {
            final Long srcTSId = srcSR.getStepId();
            String srcTsUniqueKey = null;
            for (final TestStep srcTS : srcTestSteps) {
                if (srcTS.getId() == srcTSId) {
                    srcTsUniqueKey = srcTS.getUniqueKey();
                }
            }
            if (srcTsUniqueKey != null) {
                // found it, look for it in dest
                for (final TestStep destTS : destTestSteps) {
                    if (srcTsUniqueKey.equals(destTS.getUniqueKey())) {

                        final StepResult destSR = findStepResults(destStepResults, destTS);
                        if (destSR != null) {
                            udpateStepResultStatus(destSR.getId().toString(), srcSR.getStatusUpdateJsonString(), destClient);
                        } else {
                            logger.warn("Step results are created once and execution is created.  It's not valid to create results without the test.");
                            // createNewStepResult(destClient, destIssueId, srcSR, destTS, {{executionId}});
                        }
                    }
                }
            }
        }
    }

    private void udpateStepResultStatus(final String stepResultId, final String statusUpdateString, final ZapiRestClient destClient) {
        final BasicRestResponse putResponse = destClient.updateStepResult(statusUpdateString, stepResultId);
        logger.trace("Status: [{}], Response: [{}]", putResponse.getStatusCode(), putResponse.getResponse());
    }

    private StepResult findStepResults(final ImmutableCollection<StepResult> destStepResults, final TestStep destTS) {
        for (final StepResult destSR : destStepResults) {
            if (destSR.getStepId() == destTS.getId()) {
                return destSR;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private void createNewStepResult(final ZapiRestClient destClient, final Long destIssueId, final StepResult srcSR, final TestStep destTS,
            final Long executionId) {
        final Long stepId = destTS.getId();
        final String status = srcSR.getStatus();
        final StepResult newStepResult = new StepResult(stepId, destIssueId, executionId, status);
        final BasicRestResponse postResponse = destClient.createIssueTestStepResult(newStepResult.getPostJsonString());
        logger.trace("Post Status: {}", postResponse.getStatusCode());
        logger.trace("Post Response: {}", postResponse.getResponse());
    }

    private ImmutableMap<Long, StepResult> getStepResults(final ZapiRestClient client, final Long issueId, final ImmutableCollection<Execution> executions) {
        final Map<Long, StepResult> stepResults = new HashMap<Long, StepResult>();

        // for each execution get the SR ids
        for (final Execution exec : executions) {
            try {
                final BasicRestResponse getResponse = client.getIssueTestStepResultByExecutionId(exec.getId());
                final JSONArray stepResultsList = new JSONArray(getResponse.getResponse());
                for (int i = 0; i < stepResultsList.length(); i++) {
                    final JSONObject stepResultJSONObject = stepResultsList.getJSONObject(i);
                    final StepResult newStepResult = new StepResult(stepResultJSONObject, issueId);
                    stepResults.put(newStepResult.getId(), newStepResult);
                }

            } catch (final JSONException e) {
                logger.info("{}", e.getMessage(), e);
            }

        }

        return ImmutableMap.copyOf(stepResults);
    }

    private ImmutableMap<Long, Execution> getExecutions(final ZapiRestClient client, final Long issueId) {
        final Map<Long, Execution> executions = new HashMap<Long, Execution>();
        try {
            final BasicRestResponse getResponse = client.getExecutionsByIssueId(issueId);
            final JSONObject jsonObj = new JSONObject(getResponse.getResponse());
            final JSONArray executionsList = jsonObj.getJSONArray("executions");
            for (int i = 0; i < executionsList.length(); i++) {
                final JSONObject executionJSONObject = executionsList.getJSONObject(i);
                final Execution newExecution = new Execution(executionJSONObject);
                executions.put(newExecution.getId(), newExecution);
            }
        } catch (final JSONException e) {
            logger.info("{}", e.getMessage(), e);
        }

        return ImmutableMap.copyOf(executions);
    }

    private ImmutableMap<Long, TestStep> getTestSteps(final ZapiRestClient client, final Long issueId) {
        final Map<Long, TestStep> testSteps = new HashMap<Long, TestStep>();

        try {
            final BasicRestResponse getResponse = client.getIssueTestSteps(issueId);
            final JSONArray jsonArray = new JSONArray(getResponse.getResponse());
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject jsonObj = jsonArray.getJSONObject(i);
                final TestStep newTestStep = new TestStep(jsonObj);
                testSteps.put(newTestStep.getId(), newTestStep);
            }
        } catch (final JSONException e) {
            logger.info("{}", e.getMessage(), e);
        }

        return ImmutableMap.copyOf(testSteps);
    }

    /**
     * create executions for each issue as needed
     * 
     * @param destExecutions
     * @param srcExecutions
     * @param srcClient
     * @param destIssueId
     * @return
     * @throws JSONException
     */
    private void updateExecutions(final ImmutableCollection<Execution> destExecutions, final ImmutableCollection<Execution> srcExecutions,
            final ZapiRestClient destClient, final Long destIssueId) throws JSONException {

        final ImmutableSet<String> destUniqueKeys = generateUniqueKeys(destExecutions);

        for (final Execution srcExec : srcExecutions) {
            final String uniqueKey = srcExec.getUniqueKey();
            if ((uniqueKey == null) || (destUniqueKeys == null) || !destUniqueKeys.contains(uniqueKey)) {
                final BasicRestResponse postResponse = createExecution(destIssueId, destExecutions, srcExec, destClient);
                logger.trace("Status: [{}], Response: [{}]", postResponse.getStatusCode(), postResponse.getResponse());

                if (HTTP_OK == postResponse.getStatusCode()) {
                    final String executionId = getExecutionId(postResponse.getResponse());
                    if ((postResponse.getStatusCode() == 200) && (executionId != null)) {
                        updateExecutionStatus(destClient, srcExec.getStatusUpdateJsonString(), executionId);
                    }
                }
            }
        }
    }

    private BasicRestResponse createExecution(final Long destIssueId, final ImmutableCollection<Execution> destExecutions, final Execution srcExecution,
            final ZapiRestClient destClient) throws JSONException {

        final Long projectId = getProjectId(configuration.getProjectKey(JiraRepoType.DESTINATION));
        final String assignee = getAssignee(srcExecution.getAssignee(), configuration.getJiraUsername(JiraRepoType.DESTINATION));
        final Execution newDestExecution = new Execution(srcExecution, projectId, assignee, destIssueId);
        final String jsonString = newDestExecution.getPostJsonString();
        final BasicRestResponse postResponse = destClient.createExecution(jsonString);
        return postResponse;
    }

    private void updateExecutionStatus(final ZapiRestClient destClient, final String statusUpdateString, final String executionId) throws JSONException {
        if (executionId != null) {
            final BasicRestResponse putResponse = destClient.updateExecution(statusUpdateString, executionId);
            logger.trace("Status: [{}], Response: [{}]", putResponse.getStatusCode(), putResponse.getResponse());
        } else {
            logger.info("Unable to update execution status");
        }
    }

    private String getExecutionId(final String postResponse) {
        try {
            final JSONObject jsonObj = new JSONObject(postResponse);
            final JSONArray names = jsonObj.names();
            final String key = names.getString(0);
            final JSONObject jsonData = jsonObj.getJSONObject(key);
            final String executionId = jsonData.getString("id");
            return executionId;
        } catch (final JSONException e) {
            logger.info("{}", e.getMessage(), e);
        }
        return null;
    }

    private Long getProjectId(final String projectKey) {
        final JiraRestClient client = jiraDestConnector.createJiraRestClient();
        final ProjectRestClient projectClient = client.getProjectClient();
        return projectClient.getProject(projectKey).claim().getId();
    }

    private String getAssignee(final String srcAssignee, final String jiraUser) {
        // TODO: use the execution assignee if possible
        return jiraUser;
    }

    /**
     * create test steps for each issue
     * 
     * @param destClient
     * 
     * @param destIssueId
     * @param srcIssueId
     * @param srcClient
     * @param destClient
     * @param destIssueId
     * @throws JSONException
     */
    private void updateTestSteps(final ImmutableCollection<TestStep> destTestSteps, final ImmutableCollection<TestStep> srcTestSteps,
            final ZapiRestClient destClient, final Long destIssueId) throws JSONException {

        final ImmutableSet<String> destUniqueKeys = generateUniqueKeys(destTestSteps);

        for (final TestStep ts : srcTestSteps) {
            final String uniqueKey = ts.getUniqueKey();
            if ((uniqueKey == null) || (destUniqueKeys == null) || !destUniqueKeys.contains(uniqueKey)) {
                final BasicRestResponse postResponse = destClient.createIssueTestSteps(destIssueId, ts.getPostJsonString());
                logger.trace("Post Status: {}", postResponse.getStatusCode());
                logger.trace("Post Response: {}", postResponse.getResponse());
            }
        }
    }

    private ImmutableSet<String> generateUniqueKeys(final ImmutableCollection<? extends ZephyrData> zephyrDataSet) {
        final Set<String> uniqueKeys = new HashSet<String>();
        for (final ZephyrData dataPoint : zephyrDataSet) {
            uniqueKeys.add(dataPoint.getUniqueKey());
        }
        try {
            return ImmutableSet.copyOf(uniqueKeys);
        } catch (final Exception e) {
            return null;
        }
    }

    private ImmutableMap<Long, Long> uploadDestIssues(final JiraProjectMetadataMap srcToDestMetadataMap, final ImmutableSet<Issue> sourceIssueSet) {
        final ImmutableMap<String, String> srcToDestMap = generateMapOfImportedIssuesByDestSrcKeys();
        final String foreignKeyFieldId = getForeignKeyFieldId();
        return jiraDestConnector.uploadIssues(sourceIssueSet, srcToDestMap, srcToDestMetadataMap, foreignKeyFieldId,
                jiraSourceConnector.createJiraRestClient());
    }

    private ImmutableSet<Issue> getSourceIssues() {
        final ImmutableMap<String, Issue> sourceIssuesMap = jiraSourceConnector.getIssuesByJql(configuration.getSourceJqlIssues());
        final Set<Issue> issueSet = new HashSet<Issue>();
        for (final String jiraKey : sourceIssuesMap.keySet()) {
            final Issue srcIssue = jiraSourceConnector.getIssueByKey(jiraKey);
            issueSet.add(srcIssue);
        }
        return ImmutableSet.copyOf(issueSet);
    }

    private ImmutableSet<Issue> getSourceTestCases() {
        final ImmutableMap<String, Issue> sourceTestcaseMap = jiraSourceConnector.getIssuesByJql(configuration.getSourceJqlTestcases());
        final Set<Issue> issueSet = new HashSet<Issue>();
        for (final String jiraKey : sourceTestcaseMap.keySet()) {
            final Issue srcIssue = jiraSourceConnector.getIssueByKey(jiraKey);
            issueSet.add(srcIssue);
        }
        return ImmutableSet.copyOf(issueSet);
    }

    private String getForeignKeyFieldId() {
        final String foreignKeyFieldName = configuration.getJiraSrcRefKey();
        for (final Field curr : jiraDestConnector.getProjectFields()) {
            if (foreignKeyFieldName.equals(curr.getName())) {
                return curr.getId().toString();
            }
        }
        return null;
    }

    private ImmutableMap<String, String> generateMapOfImportedIssuesByDestSrcKeys() {
        final String foreignKeySearchString = generateForeignKeySearchString();
        final ImmutableMap<String, Issue> destIssues = jiraDestConnector.getIssuesByJql(foreignKeySearchString);
        final Map<String, String> destIssueKeyToSrcIssueKeyMap = new HashMap<String, String>();
        for (final Map.Entry<String, Issue> curr : destIssues.entrySet()) {
            destIssueKeyToSrcIssueKeyMap.put(getJiraSrcRefKey(curr), curr.getKey());
        }
        return ImmutableMap.copyOf(destIssueKeyToSrcIssueKeyMap);
    }

    private String generateForeignKeySearchString() {
        return "project = " + configuration.getProjectKey(JiraRepoType.DESTINATION) + " and \"" + configuration.getJiraSrcRefKey() + "\" is not EMPTY";
    }

    private String getJiraSrcRefKey(final Map.Entry<String, Issue> curr) {
        return curr.getValue().getFieldByName(configuration.getJiraSrcRefKey()).getValue().toString();
    }

}
