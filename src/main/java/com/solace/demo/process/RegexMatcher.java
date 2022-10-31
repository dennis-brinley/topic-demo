package com.solace.demo.process;

import java.util.ArrayList;
import java.util.List;

import com.solace.demo.model.match.MatchConfig;
import com.solace.demo.model.match.RegexPatternEntry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegexMatcher {

    @Getter
    private List<RegexMatcherEntry> entries = new ArrayList<RegexMatcherEntry>();

    /**
     * Initialize the RegexMatcher
     * @param matchConfig - Object defining configuration settings for the matcher
     * This parameter is created by reading a yaml configuration file
     */
    public RegexMatcher(MatchConfig matchConfig) {
        Integer     min = matchConfig.getExpressions().get(0).getPriority(), 
                    max = matchConfig.getExpressions().get(0).getPriority();

        log.info("Configuring RegexMatcher with REGEX entries");
        // GET MIN/MAX PRIORITY FOR SORTING
        for ( RegexPatternEntry rpe : matchConfig.getExpressions() ) {
            if ( rpe.getPriority() > max ) {
                max = rpe.getPriority();
            } 
            if ( rpe.getPriority() < min ) {
                min = rpe.getPriority();
            }
        }

        for ( Integer i = min; i <= max; i++ ) {
            for ( RegexPatternEntry rpe : matchConfig.getExpressions() ) {
                if ( rpe.getPriority() == i ) {
                    entries.add( 
                        new RegexMatcherEntry(
                            rpe.getName(), 
                            rpe.getRegexPattern(), 
                            rpe.getPriority(),
                            ( rpe.getTopicDomain() == null ? "" : rpe.getTopicDomain() )));
                            
                    log.info("Configured REGEX [{}] for topicDomain [{}]; priority = {}", 
                            rpe.getName(), 
                            (rpe.getTopicDomain() == null ? "MATCH ANY" : rpe.getTopicDomain()), 
                            rpe.getPriority() );
                }
            }
        }

        log.info("RegexMatcher has been configured with {} REGEX entries", entries.size());
    }

    /***
     * Checks a given string for matches against identified regular expressions
     * @param s - The string to match against RegEx expressions configured for the matcher
     * @param topicDomain - The topic domain that this topic belongs to. 
     * Selector for domain-specific regex expressions. RegexMatcherEntries not identified
     * with a domain will also be used for matching.
     * @return - RegexMatcherEntry object if a match was found, NULL if a match was not found
     */
    public RegexMatcherEntry checkForMatch( String s, String topicDomain ) {
        for ( RegexMatcherEntry e : this.entries ) {

            //  DO NOT MATCH IF THE REGEX BELONGS TO A TOPIC DOMAIN THAT
            //  THIS PROCESS IS NOT
            if ( !e.getTopicDomain().contentEquals("") 
                 && !e.getTopicDomain().contentEquals(topicDomain)) {
                continue;
            }

            if ( e.matches(s) ) return e;
        }
        return null;
    }
}
