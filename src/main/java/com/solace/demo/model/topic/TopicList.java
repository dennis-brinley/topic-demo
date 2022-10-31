package com.solace.demo.model.topic;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is to read topics from a yaml file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicList {

    /***
     * Array list of topics read from a yaml file
     */
    @Builder.Default
    protected List<String> topics = new ArrayList<String>();
}