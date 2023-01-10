package com.solace.demo.process;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.solace.demo.model.match.MatchConfig;
import com.solace.demo.model.topic.TopicList;
import com.solace.demo.model.topic.TopicNode;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicProcessor {

    /***
     * Top-level tree structure where topic nodes are stored
     */
    @Getter
    private List<TopicNode> rootTopicNodes = new ArrayList<TopicNode>();

    /**
     * Topics that that caused an exception to be thrown and could not
     * be fully evaluated
     */
    @Getter
    private List<String> errorTopics = new ArrayList<String>();

    private MatchConfig matchConfig = null;

    private RegexMatcher regexMatcher = null;

    /**
     * Total number of topics processed, including failures
     */
    @Getter
    private long processedTopicCount = 0;

    /**
     * Cumulative time performing match operations for a TopicProcessor object instance.
     * In nanoseconds
     */
    @Getter
    private long cumulativeElapsedProcessingTimeNano = 0;
    
    /**
     * Constructor
     * @param matchConfig
     */
    public TopicProcessor(MatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        regexMatcher = new RegexMatcher(matchConfig);
    }
    
    /**
     * Process all topics provided as a list from input json/yaml file
     * @param topicList
     * @throws Exception
     */
    public void processTopics(TopicList topicList) throws Exception {

        if ( topicList == null || matchConfig == null ) {
            log.error("Attempted to execute processTopics() with one or both matchConfig and topicList == null");
            throw new Exception("topicList and matchConfig are required " +
                                "to be set (Non-null) prior to calling processTopics()");
        }
        
        for (String topic : topicList.getTopics() ) {
            processTopic( topic );
        }

    }

    /**
     * Process an individual topic and include it in the TopicNode tree
     * @param topic
     * @throws Exception
     */
    public void processTopic(String topic) throws Exception {

        long startNano = System.nanoTime();
        try {
            StringTokenizer tokenizer = new StringTokenizer(topic, matchConfig.getTopicDelimiter());
            execMatch(tokenizer, rootTopicNodes, "" );
        } catch ( Exception exc ) {
            log.warn("Error processing topic string: {}", topic);
            errorTopics.add( topic );
        }
        processedTopicCount++;
        cumulativeElapsedProcessingTimeNano += System.nanoTime() - startNano;
    }

    /**
     * Called recursively by 'processTopic' for each node in the topic string
     * Matches to existing TopicNode or creates a new one if no match
     * @param tokenizer - Tokenized topic string
     * @param topicNodes - List of TopicNode objects that may match the current node
     * @param topicDomain - The first element discovered for the topic; Used to scope subsequent REGEX expressions
     * @throws Exception
     */
    private void execMatch(StringTokenizer tokenizer, List<TopicNode> topicNodes, String topicDomain) throws Exception {

        // SAFETY CHECK
        if (!tokenizer.hasMoreTokens()) return;
        String currentNodeString = tokenizer.nextToken();
        String td = topicDomain;

        //  1. CHECK FOR MATCH IN EXISTING NODES
        for (TopicNode topicNode : topicNodes) {
            if (topicNode.matches(currentNodeString)) {
                topicNode.incrementMatchCount();
                if ( td.length() == 0 && !topicNode.isVariableNode() ) {
                    td = topicNode.getName();
                }
                if (tokenizer.hasMoreTokens()) {
                    execMatch(tokenizer, topicNode.getTopicNodes(), td);
                } else {
                    topicNode.incrementTermCount();
                }
                return;
            }
        }

        //  2. CHECK FOR MATCH IN REGEX EXPRESSIONS
        RegexMatcherEntry re = regexMatcher.checkForMatch(currentNodeString, td);
        if ( re != null ) {
            //  A. FOUND IN REGEX, CREATE NEW VARIABLE NODE
            TopicNode newVariableNode = new TopicNode(re.getName(), re.getRegexPattern(), true );
            newVariableNode.incrementMatchCount();
            if (tokenizer.hasMoreTokens()) {
                execMatch(tokenizer, newVariableNode.getTopicNodes(), td);
            } else {
                newVariableNode.incrementTermCount();
            }
            topicNodes.add(newVariableNode);
        } else {
            //  B. NOT FOUND IN REGEX, CREATE NEW LITERAL NODE
            String newLiteralNodeRegex = String.format("%s%s%s", "^", escapeSpecialForRegex(currentNodeString), "$");
            TopicNode newLiteralNode = new TopicNode(currentNodeString, newLiteralNodeRegex, false);
            newLiteralNode.incrementMatchCount();
            if ( td.contentEquals("") ) {
                td = newLiteralNode.getName();
            }
            if ( tokenizer.hasMoreTokens() ) {
                execMatch(tokenizer, newLiteralNode.getTopicNodes(), td);
            } else {
                newLiteralNode.incrementTermCount();
            }
            topicNodes.add( newLiteralNode );
        }
        return;
    }

    /**
     * Recursively iterate through the rootTopicNodes and sum up all the uniqueTopicCount values
     * @return
     */
    public long getUniqueTopicCount() {

        long uniqueCount = 0;
        for ( TopicNode topicNode : this.rootTopicNodes ) {
            uniqueCount += topicNode.getUniqueTopicCount();
        }
        return uniqueCount;
    }

    /**
     * When storing REGEX for a new literal node, make sure that any REGEX special characters are escaped
     * Not 100 pct sure this is working properly
     * @param s
     * @return
     */
    private String escapeSpecialForRegex(String s) {
        final String escChars[] = { 
                "\\\\", "\\.", "\\*", "\\>", "\\+", "\\-", "\\(", "\\)", "\\^", "\\$", "\\#", "\\@", "\\!", "\\&", 
                "\\=", "\\,", "\\{", "\\}", "\\[", "\\]", "\\|", "\\\"", "\\:", "\\;", "\\~", "\\`", "\\%", "\\?", "\\<" 
            };

        for ( int i = 0; i < escChars.length; i++ ) {
            s = s.replaceAll( escChars[ i ], escChars[ i ] );
        }

        return s;
    }
}
