package com.solace.demo.model.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegexPatternEntry {

    /**
     * The name of the regular expression pattern. The value can be used
     * to define known topic strings. It is also displayed in the topic
     * list output.
     */
    @NonNull
    protected String name;

    /**
     * The regular expression
     */
    @NonNull
    protected String regexPattern;

    /**
     * Defines the order in which regular expression matches are applied.
     * Lower values are evaluated first.
    */
    @NonNull
    protected Integer priority;

    /**
     * Defines a specific top-level topic domain that a REGEX may apply to
     */
    protected String topicDomain;
}
