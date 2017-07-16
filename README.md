# Jira Migration Tool

## JMT is a java program for migrating content from one Jira instance to another

## Sample use

### Command line with configuration .properties file
Parameters for program can be defined in .properties file defined by cli parameter named _configurationFilePath_

	java -jar target/jira-migration-tool-1.0-SNAPSHOT.jar -configurationFilePath ./configuration.properties
sample configuration.properties file can be found in src/test/resources/configuration.properties


## Parameters overview

| .properties params name   | description         | example value |
 -------------------------- | ----------------------------- | ------------------  | ------ |
| n/a    					| path to properties file with configuration params | ./../configuration.properties |
| jira.url.source      			| -jiraUrl 						| URL to jira | https://ensemble.atlassian.net |
| jira.username.source   			| Jira username | johnny  |
| jira.password.source   			| Jira password | passw0rd123  |
| jira.projectKey.source   			| Jira project key | HA  |
| jira.url.dest			| -jiraUrl 						| URL to jira | https://ensemble.atlassian.net |
| jira.username.dest   			| Jira username | johnny  |
| jira.password.dest   			| Jira password | passw0rd123  |
| jira.projectKey.dest   			| Jira project key | HA |

| jira.source.jql.issues   			| JQL query for source issues | project = HA  and issuetype != Test |
| jira.source.jql.testcases   			| JQL query for source testcases | project = HA  and issuetype = Test |
| jira.searchLimit.issues   			| limit on issue migrated | 100 (Default is 50) |
| jira.searchLimit.testcases   			| limit on testcases migrated | 100 (Default is 50) |
| jira.srcRefKey   			| destination field key used to hold reference to source issue/test | FOR id |
| jira.field.blackList   			| list of field ids to skip | resolutiondate,workratio,lastViewed |

