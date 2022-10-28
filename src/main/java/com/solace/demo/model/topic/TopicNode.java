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
    private long matchCount = 0;

    /***
     * Count of the number of topics where this was the last node in the topic
     * If termCount > 0, then this will identified as the last node in a
     * full topic string
     */
    @Getter
    private long termCount = 0;

    private Pattern p;

    /***
     * Nested container for sub-nodes
     */
    @Getter
    protected List<TopicNode> topicNodes = new ArrayList<TopicNode>();

    public TopicNode(   String      name,
                        String      regexPattern,
                        boolean     variableNode    ) {
        
        this.name = name;
        this.regexPattern = regexPattern;
        this.variableNode = variableNode;

        p = Pattern.compile(regexPattern);
    }

    public void incrementMatchCount() {
        matchCount++;
    }

    public void incrementTermCount() {
        termCount++;
    }

    public boolean matches(String s) {
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public void displayTopicTree( String delim ) {
        String rootTopic = "";
        displayTopicTreeElements( this, rootTopic, delim );
    }

    public long getUniqueTopicCount() {
        long uniqueCount = 0;
        if ( this.termCount > 0 ) uniqueCount++;
        for ( TopicNode topicNode : this.topicNodes ) {
            uniqueCount += topicNode.getUniqueTopicCount();
        }
        return uniqueCount;
    }

    private void displayTopicTreeElements( TopicNode node, String rootTopic, String delim ) {
        String topicString = rootTopic + ( node.isVariableNode() ? String.format("{%s}", node.getName() ) : node.getName() );
        if ( node.getTermCount() > 0 ) {
            System.out.println(String.format("%06d -- %s", node.getTermCount(), topicString) );
        }
        for (TopicNode subNode : node.getTopicNodes() ) {
            displayTopicTreeElements( subNode, topicString + delim, delim );
        }
    }
}
