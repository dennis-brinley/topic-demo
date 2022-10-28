package com.solace.demo;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.solace.demo.model.match.MatchConfig;
import com.solace.demo.model.topic.TopicList;
import com.solace.demo.model.topic.TopicNode;
import com.solace.demo.process.TopicProcessor;

import com.solace.messaging.MessagingService;
import com.solace.messaging.config.SolaceProperties.AuthenticationProperties;
import com.solace.messaging.config.SolaceProperties.ServiceProperties;
import com.solace.messaging.config.SolaceProperties.TransportLayerProperties;
import com.solace.messaging.config.profile.ConfigurationProfile;
import com.solace.messaging.receiver.DirectMessageReceiver;
import com.solace.messaging.receiver.MessageReceiver.MessageHandler;
import com.solace.messaging.resources.TopicSubscription;

import lombok.extern.slf4j.Slf4j;

/**
 * Command Line executable class for TopicProcessor using RegexMatcher
 */
@Slf4j
public class CrushIt 
{
    static final String		ARG_TOPICS_FILE_IN  = "--topics-file-in=";

    static final String     ARG_SOLACE_HOST     = "--solace-broker=";
    static final String     ARG_SOLACE_VPN      = "--vpn=";
    static final String     ARG_BASIC_USER      = "--basic-user=";
    static final String     ARG_BASIC_PWD       = "--basic-password=";

    static final String     ARG_WAIT_MILLIS     = "--wait-millis=";
    static final String		ARG_SUBSCRIPTION   	= "--output-file=";

    public static void main( String[] args ) throws IOException
    {

        ObjectMapper mapper = new ObjectMapper( new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES) );
    	
        MatchConfig matchConfig = null;
        String matchConfigYamlFile = "src/main/resources/match/regex.yaml";

        String  // topicsFileInput = "",               // NOT USED
                solaceMsgHost = "",
                solaceVpn = "",
                solaceBasicUser = "",
                solaceBasicPassword = "",
                subscription = ">";

        Long    waitMillis = 5000L;

        try {
            for (String arg : args) {
                // if (arg.startsWith(ARG_TOPICS_FILE_IN) && arg.length() > ARG_TOPICS_FILE_IN.length()) {
                //     topicsFileInput = arg.replace(ARG_TOPICS_FILE_IN, "");
                //     continue;
                // }
                if (arg.startsWith(ARG_SOLACE_HOST) && arg.length() > ARG_SOLACE_HOST.length()) {
                    solaceMsgHost = arg.replace(ARG_SOLACE_HOST, "");
                    continue;
                }
                if (arg.startsWith(ARG_SOLACE_VPN) && arg.length() > ARG_SOLACE_VPN.length()) {
                    solaceVpn = arg.replace(ARG_SOLACE_VPN, "");
                    continue;
                }
                if (arg.startsWith(ARG_BASIC_USER) && arg.length() > ARG_BASIC_USER.length()) {
                    solaceBasicUser = arg.replace(ARG_BASIC_USER, "");
                    continue;
                }
                if (arg.startsWith(ARG_BASIC_PWD) && arg.length() > ARG_BASIC_PWD.length()) {
                    solaceBasicPassword = arg.replace(ARG_BASIC_PWD, "");
                    continue;
                }
                if (arg.startsWith(ARG_SUBSCRIPTION) && arg.length() > ARG_SUBSCRIPTION.length()) {
                    subscription = arg.replace(ARG_SUBSCRIPTION, "");
                    continue;
                }
                if (arg.startsWith(ARG_WAIT_MILLIS) && arg.length() > ARG_WAIT_MILLIS.length()) {
                    waitMillis = Long.parseLong( arg.replace(ARG_WAIT_MILLIS, "") );
                    continue;
                }
            }
        } catch ( Exception exc ) {
            log.error( "Error reading arguments: {}", exc.getMessage() );
            System.exit(-1);
        }

        // READ MATCHER CONFIG FILE
        try {
        	matchConfig = mapper.readValue(new File(matchConfigYamlFile), MatchConfig.class);
        } catch (DatabindException dbexc) {
            log.error("Failed to parse the input file: {}", matchConfigYamlFile);
            log.error("Parsing error: {}\n{}", dbexc.getMessage(), dbexc.getStackTrace());
        	System.exit(-10);
		} catch (StreamReadException srexc ) {
            log.error("Failed to parse the input file: {}", srexc.getMessage());
            log.error("Parsing error: {}\n{}", srexc.getMessage(), srexc.getStackTrace());
        	System.exit(-11);
		} catch (IOException ioexc) {
            log.error("There was an error reading the input file: {}", matchConfigYamlFile);
            log.error("Parsing error: {}\n{}", ioexc.getMessage(), ioexc.getStackTrace());
        	System.exit(-12);
		}
		log.info("PARSING CONFIG FILE COMPLETE: {}", matchConfigYamlFile);
        log.info("First Entry Name: {} --- Pattern: {}", 
                    matchConfig.getExpressions().get(0).getName(), 
                    matchConfig.getExpressions().get(0).getRegexPattern() );

/***
        //  ### READ TOPICS FROM FILES INSTEAD OF SERVER
        TopicList topicList = null;
        String topicListYamlFile = "src/test/resources/topic-list-02.yaml";
        try {
            topicList = mapper.readValue(new File(topicListYamlFile), TopicList.class);
        } catch ( Exception exc ) {
            log.error("Parse File failed: {}", exc.getMessage());
            exc.printStackTrace();
            //log.error("{}", exc.);
        }
        log.info("PARSING TOPICS FILE COMPLETE: {}", topicListYamlFile);
        log.info("First topic: {}", topicList.getTopics().get(0));
*/

        //  CACHE FOR TOPICS LIST; CURRENTLY NOT THREAD SAFE!!!
        TopicList topicList = new TopicList();


        final Properties properties = new Properties();
        properties.setProperty(TransportLayerProperties.HOST, solaceMsgHost);                          // protocol://host:port
        properties.setProperty(ServiceProperties.VPN_NAME,  solaceVpn);                                // message-vpn
        properties.setProperty(AuthenticationProperties.SCHEME_BASIC_USER_NAME, solaceBasicUser);      // client-username
        properties.setProperty(AuthenticationProperties.SCHEME_BASIC_PASSWORD, solaceBasicPassword);   // client-password
        properties.setProperty(ServiceProperties.RECEIVER_DIRECT_SUBSCRIPTION_REAPPLY, "true"); // subscribe Direct subs after reconnect

        final MessagingService messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(properties).build().connect();  // blocking connect to the broker
        final DirectMessageReceiver receiver = messagingService.createDirectMessageReceiverBuilder()
                .withSubscriptions(TopicSubscription.of( subscription )).build().start();
        
        //  START SUBSCRIBING AND CACHING MSGS
        final MessageHandler messageHandler = (inboundMessage) -> {
           // log.debug( "TOPIC: {}", inboundMessage.getDestinationName() );
            topicList.getTopics().add(inboundMessage.getDestinationName());
        };
        receiver.receiveAsync(messageHandler);

        // WAIT FOR PRESCRIBED TIME
        try {
            Thread.sleep( waitMillis );
        } catch ( RuntimeException rtexc ) {
            log.error("Run time exception???");
        } catch ( InterruptedException iexc ) {
            log.error("Thread sleep interrupted - probably shutting down");
        }

        //  DISCONNECT FROM THE BROKER
        receiver.terminate(500);
        messagingService.disconnect();
        log.info("Disconnecting from Solace Broker");


        final long beginTime = System.currentTimeMillis();

        TopicProcessor topicProcessor = new TopicProcessor(matchConfig);
        try { 
            topicProcessor.processTopics( topicList );
        } catch ( Exception exc ) {
            log.error(exc.getMessage());
            return;
        }
        final long endTime = System.currentTimeMillis();

        //  DISPLAY RESULTS
        for ( TopicNode rootNode : topicProcessor.getRootTopicNodes() ) {
            rootNode.displayTopicTree( matchConfig.getTopicDelimiter() );
        }

        log.info( "Total Topics Processed: {}", topicList.getTopics().size() );
        log.info( "Elapsed Time: {} milliseconds", ( endTime - beginTime) );

        return;
    }
}
