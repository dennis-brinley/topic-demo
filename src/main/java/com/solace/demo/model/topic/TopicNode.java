package com.solace.demo.model.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;

public class TopicNode {
    
    /***
     * Name of the detected topic node
     * If none of the defined regex patterns match, the name will be set to the data
     * contained in the element.
     * If a regex pattern matches, then the name will be set to the name of the
     * matching pattern in braces
     * 
     * e.g. node = DELIVERY, name --> DELIVERY
     *      node = false, name --> {BOOLEAN}
     */
    @Getter
    protected String name;

    /***
     * The regex pattern that is used to identify matching nodes
     * If none of the defined regex patterns match, then the regexPattern will be set
     * to the text of the input node with start+end anchors. This is done to
     * prevent partial matches
     * 
     * e.g. node = DELIVERY, regexPattern --> ^DELIVERY$
     */
    @Getter
    protected String regexPattern;

    /***
     * Set to true if this node is detected as a variable
     * Node is detected as a variable if it matches a defined regex pattern
     */
    @Getter
    private boolean variableNode;

    /***
     * Number of matches reported on this node
     */
    @Getter
    private long matchCount = 0L;

    /***
     * Count of the number of topics where this was the last node in the topic
     * If termCount > 0, then this will identified as the last node in a
     * full topic string
     */
    @Getter
    private long termCount = 0L;

    /**
     * Compiled regex pattern
     */
    private Pattern p;

    /***
     * Nested container for sub-nodes
     */
    @Getter
    protected List<TopicNode> topicNodes = new ArrayList<TopicNode>();

    /**
     * Constructor
     * @param name
     * @param regexPattern - RegEx pattern used to identify the topic node on the tree
     * @param variableNode - TRUE if matches a configured RegEx pattern; FALSE if a literal string
     */
    public TopicNode(   String      name,
                        String      regexPattern,
                        boolean     variableNode    ) {
        
        this.name = name;
        this.regexPattern = regexPattern;
        this.variableNode = variableNode;

        p = Pattern.compile(regexPattern);
    }

    /**
     * Invoke when a match found on node
     */
    public void incrementMatchCount() {
        matchCount++;
    }

    /**
     * Invoke when the node is the last leaf.
     * Identifies a completed topic string
     */
    public void incrementTermCount() {
        termCount++;
    }

    /**
     * Identifies if this tree node REGEX matches the topic node (String s) passed as input
     * @param s - Topic Node String (single element of a topic)
     * @return TRUE if matches; FALSE if does not match
     */
    public boolean matches(String s) {
        Matcher m = p.matcher(s);
        return m.matches();
    }

    /**
     * Get the total count of unique topics associated with this node and all nested sub-nodes
     * @return
     */
    public long getUniqueTopicCount() {
        long uniqueCount = 0L;
        if ( this.termCount > 0L ) uniqueCount++;
        for ( TopicNode topicNode : this.topicNodes ) {
            uniqueCount += topicNode.getUniqueTopicCount();
        }
        return uniqueCount;
    }

    /**
     * Display the topic tree for the current node and all sub-nodes
     * @param delim
     */
    public void displayTopicTree( String delim ) {
        String rootTopic = "";
        displayTopicTreeElements( this, rootTopic, delim );
    }

    private void displayTopicTreeElements( TopicNode node, String rootTopic, String delim ) {
        String topicString = rootTopic + ( node.isVariableNode() ? String.format("{%s}", node.getName() ) : node.getName() );
        if ( node.getTermCount() > 0L ) {
            System.out.println(String.format("%07d -- %s", node.getTermCount(), topicString) );
        }
        for (TopicNode subNode : node.getTopicNodes() ) {
            displayTopicTreeElements( subNode, topicString + delim, delim );
        }
    }

    /**
     * Return the processed topic tree as a String list
     * @param delim - The topic delimiter; should be '/' for Solace
     * @return
     */
    public List<String> getTopicTree( String delim ) {
        String rootTopic = "";
        List< String > topicTree = new ArrayList<String>();
        for ( String topic : this.getTopicListTreeElements( this, rootTopic, delim ) ) {
            topicTree.add(topic);
        }
        return topicTree;
    }

    /**
     * Formats topic strings from the TopicNode tree
     * @param node
     * @param rootTopic
     * @param delim
     * @return
     */
    private List<String> getTopicListTreeElements( TopicNode node, String rootTopic, String delim ) {
        List< String > topicList = new ArrayList<String>();
        String topicString = rootTopic + ( node.isVariableNode() ? String.format("{%s}", node.getName() ) : node.getName() );
        if ( node.getTermCount() > 0L ) {
            topicList.add(topicString);
        }
        for (TopicNode subNode : node.getTopicNodes()) {
            for ( String topic : subNode.getTopicListTreeElements(subNode, topicString + delim, delim)) {
                topicList.add(topic);
            }
        }
        return topicList;
    }
}
