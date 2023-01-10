package com.solace.demo.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

public class RegexMatcherEntry {

    /**
     * The name of the regular expression pattern. The value can be used
     * to define known topic strings. It is also displayed in the topic
     * list output.
     */
    @Getter
    protected String name;

    /**
     * The regular expression
     */
    @Getter
    protected String regexPattern;

    /**
     * Defines the order in which regular expression matches are applied.
     * Lower values are evaluated first.
    */
    @Getter
    protected Integer priority;

    /**
     * The first literal element of a discovered topic. This element is used as
     * a selector to identify domain-specific Regex expressions that will apply
     * to the topic.
     */
    @Getter
    protected String topicDomain;

    private Pattern p;

    /**
     * Create RegexMatcher Entry - Equates to REGEX pattern that will be used to build the TopicNode tree
     * @param name
     * @param regexPattern
     * @param priority
     * @param topicDomain
     */
    protected RegexMatcherEntry(String name, String regexPattern, Integer priority, String topicDomain) {
        this.name = name;
        this.regexPattern = regexPattern;
        this.priority = priority;
        this.topicDomain = topicDomain;
        this.p = Pattern.compile(regexPattern);
    }

    /**
     * Check if the parameter String s is a match to the REGEX expression
     * @param s
     * @return
     */
    protected boolean matches(String s) {
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
