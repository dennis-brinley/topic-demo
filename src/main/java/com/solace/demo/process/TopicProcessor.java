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

    @Getter
    private List<String> errorTopics = new ArrayList<String>();

    private MatchConfig matchConfig = null;

    private RegexMatcher regexMatcher = null;
    
    public TopicProcessor(MatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        regexMatcher = new RegexMatcher(matchConfig);
    }
    
    public void processTopics(TopicList topicList) throws Exception {

        if ( topicList == null || matchConfig == null ) {
            log.error("Attempted to execute processTopics() with one or both matchConfig and topicList == null");
            throw new Exception("topicList and matchConfig are required " +
                                "to be set (Non-null) prior to calling processTopics()");
        }
        
        for (String processTopic : topicList.getTopics() ) {
            try {
                StringTokenizer tokenizer = new StringTokenizer(processTopic, matchConfig.getTopicDelimiter());
                if (tokenizer.hasMoreTokens()) {
                    execMatch(tokenizer, rootTopicNodes, "" );
                }
            } catch ( Exception exc ) {
                log.warn("Error processing topic string: {}", processTopic);
                errorTopics.add( processTopic );
            }
        }
    }

    private void execMatch(StringTokenizer tokenizer, List<TopicNode> topicNodes, String topicDomain) throws Exception {

        // SAFETY CHECK
        if (!tokenizer.hasMoreTokens()) return;
        String currentNodeString = tokenizer.nextToken();
        String td = topicDomain;

        for (TopicNode topicNode : topicNodes) {
            //  CHECK FOR MATCH AT THE CURRENT LEVEL
            if (topicNode.matches(currentNodeString)) {
                topicNode.incrementMatchCount();
                if ( td.contentEquals("") && !topicNode.isVariableNode() ) {
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

        //  CHECK FOR MATCH IN REGEX EXPRESSIONS
        RegexMatcherEntry re = regexMatcher.checkForMatch(currentNodeString, td);
        if ( re != null ) {
            //  FOUND IN REGEX, CREATE NEW VARIABLE NODE
            TopicNode newVariableNode = new TopicNode(re.getName(), re.getRegexPattern(), true );
            newVariableNode.incrementMatchCount();
            if (tokenizer.hasMoreTokens()) {
                execMatch(tokenizer, newVariableNode.getTopicNodes(), td);
            } else {
                newVariableNode.incrementTermCount();
            }
            topicNodes.add(newVariableNode);
        } else {
            //  NOT FOUND IN REGEX, CREATE NEW LITERAL NODE
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

    public long getUniqueTopicCount() {

        long uniqueCount = 0;
        for ( TopicNode topicNode : this.rootTopicNodes ) {
            uniqueCount += topicNode.getUniqueTopicCount();
        }
        return uniqueCount;
    }

    private String escapeSpecialForRegex(String s) {
        String escChars[] = { 
                "\\\\", "\\.", "\\*", "\\>", "\\+", "\\-", "\\(", "\\)", "\\^", "\\$", "\\#", "\\@", "\\!", "\\&", 
                "\\=", "\\,", "\\{", "\\}", "\\[", "\\]", "\\|", "\\\"", "\\:", "\\;", "\\~", "\\`", "\\%", "\\?", "\\<" 
            };

        for ( int i = 0; i < escChars.length; i++ ) {
            s = s.replaceAll( escChars[ i ], escChars[ i ] );
        }

        return s;
    }
}
