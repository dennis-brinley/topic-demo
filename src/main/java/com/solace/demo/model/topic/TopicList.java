package com.solace.demo.model.topic;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicList {
    /***
     * 
     */
    @Builder.Default
    protected List<String> topics = new ArrayList<String>();
}