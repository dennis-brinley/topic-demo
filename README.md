# topic-demo


## Configuration
Use yaml configuration file to define topic delimiter, Regex Expressions, and pre-defined topic structures.

**Note:** *Pre-defined topic structures are not currently enabled.

Example Configuration YAML file:
(../src/main/resources/match/regex.yaml)

```yaml
---
topicDelimiter: /
expressions:
  - name: BOOLEAN
    regexPattern: ^([Tt]rue|[Ff]alse|TRUE|FALSE)$
    priority: 100
  - name: REALNUM
    regexPattern: ^[\+\-]?\d+\.\d+$
    priority: 100
  - name: MYDOMAIN_COORDS
    regexPattern: ^\d{4}(N|S)\d{5}(E|W)$
    priority: 45
    topicDomain: MYDOMAIN
```

- name: RegEx pattern name. This name will be used in the output if it is identified in a topic string.
- regExPattern: The regular expression for matching. The pattern should start/end with ^/$ if you want the match to occur only if the entire topic element matches the pattern.
- priority: Sets the order in which patterns are matched to topic elements. Evaluated low to high. Negative numbers are permitted.
- topicDomain: If set, then only topics matching the topic domain will use the regular expression pattern. This is used as a selector so that regex patterns can be defined for specific topic domains.
    - "Topic Domains" are defined by the first literal node encountered in a topic during processing.
    - e.g. Topic MYDOMAIN/true/V2.1/ORDER1234/GREEN --> Topic Domain = "MYDOMAIN"

## Command Line
The following arguments are available on the command line:
- --topics-file-in=[ yaml topics file ] - Not enabled with uncommenting some code. Used to read topics from a yaml file (see test resources)
    -- See ```src/test/resources``` for example topic files
- --regex-config=[ yaml config file ]
    - Defaults to: ```src/main/resources/match/regex.yaml``` on the class path
- --solace-broker=Connection string to Solace PubSub+ broker: [ protocol://host:port ]
    - e.g. ```--solace-broker=tcps://my-super-broker.org:55443```
- --vpn=[ solace message vpn ]
- --basic-user=[ Basic Auth User ]
- --basic-password=[ Basic Auth Password ]
- --wait-millis=[ Time to listen to the broker topics in milliseconds ]
    - ```--wait-millis=5000``` --> listen for 5 seconds before disconnecting
    - Default = 5000 milliseconds
- --subscription=[ topic subscription string ]
    - ```--subscription=ACME/orders/>
    - Default = > (All topics)
