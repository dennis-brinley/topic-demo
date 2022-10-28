package com.solace.demo.model.match;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchConfig {
    
    /**
     * 
     */
    @NonNull
    protected String topicDelimiter;

    /**
     * 
     */
    @Builder.Default
    protected List<RegexPatternEntry> expressions = new ArrayList<RegexPatternEntry>();

    /**
     * 
     */
    @Builder.Default
    protected List<String> knownTopics = new ArrayList<String>();
}
