package com.infusion.relnotesgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.jira.rest.client.domain.Issue;
import com.infusion.relnotesgen.util.PredefinedDictionaryComparator;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class ReleaseNotesGenerator {

    private Configuration configuration;
    private freemarker.template.Configuration freemarkerConf;

    public ReleaseNotesGenerator(final Configuration configuration) {
        this.configuration = configuration;
        freemarkerConf = new freemarker.template.Configuration();

        freemarkerConf.setClassForTemplateLoading(ReleaseNotesGenerator.class, "/");
        freemarkerConf.setIncompatibleImprovements(new Version(2, 3, 20));
        freemarkerConf.setDefaultEncoding("UTF-8");
        freemarkerConf.setLocale(Locale.getDefault());
        freemarkerConf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        BeansWrapper beansWrapper = (BeansWrapper) ObjectWrapper.BEANS_WRAPPER;
        beansWrapper.setExposeFields(true);
        freemarkerConf.setObjectWrapper(beansWrapper);
    }

    public void generate(final Collection<Issue> issues, final File report, final String version) throws IOException {
        if(report.exists()) {
            report.delete();
        }

        Map<String, Object> input = new HashMap<String, Object>();
        input.put("issues", splitByType(issues));
        input.put("jiraUrl", configuration.getJiraUrl());
        input.put("version", version);

        Template template = freemarkerConf.getTemplate("report.ftl");

        try (Writer fileWriter = new FileWriter(report)) {
            try {
                template.process(input, fileWriter);
            } catch (TemplateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, List<Issue>> splitByType(final Collection<Issue> issues) {
        TreeMap<String, List<Issue>> issuesByType = new TreeMap<>(new PredefinedDictionaryComparator(configuration.getIssueSortType()));
        for (Issue issue : issues) {
            String issueType = issue.getIssueType().getName();
            List<Issue> typedIssues = issuesByType.get(issueType);
            if (typedIssues == null) {
                typedIssues = new ArrayList<Issue>();
                issuesByType.put(issueType, typedIssues);
            }
            typedIssues.add(issue);
        }

        IssuePriorityComparator priorityComparator = new IssuePriorityComparator(configuration.getIssueSortPriority());
        for(List<Issue> issuesByTypeList : issuesByType.values()) {
            Collections.sort(issuesByTypeList, priorityComparator);
        }
        return issuesByType;
    }

    class IssuePriorityComparator implements Comparator<Issue> {

        private PredefinedDictionaryComparator predefinedDictionaryComparator;
        private String[] typeOrder = {"Highest", "High", "Medium", "Low", "Lowest"};

        public IssuePriorityComparator(final String order) {
            if(StringUtils.isNotEmpty(order)) {
                this.typeOrder = order.split(",");
            }
            predefinedDictionaryComparator = new PredefinedDictionaryComparator(typeOrder);
        }

        @Override
        public int compare(final Issue a, final Issue b) {
            if(b.getPriority() == null) {
                return -1;
            }
            if(a.getPriority() == null) {
                return 1;
            }

            return predefinedDictionaryComparator.compare(a.getPriority().getName(), b.getPriority().getName());
        }
    }
}
