package com.solace.demo.model.match;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Defines yaml configuration file including topic delimiter, Regex Expressions,
 * and pre-defined topic strings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchConfig {
    
    /**
     * The default topic delimiter. Defaults to "/"
     */
    @NonNull
    @Builder.Default
    protected String topicDelimiter = "/";

    /**
     * Regular expression entries
     */
    @Builder.Default
    protected List<RegexPatternEntry> expressions = new ArrayList<RegexPatternEntry>();

    /**
     * Not used. Reserved for future use
     * The purpose of this list would be to pre-populate the topic node tree
     */
    @Builder.Default
    protected List<String> knownTopics = new ArrayList<String>();
}
