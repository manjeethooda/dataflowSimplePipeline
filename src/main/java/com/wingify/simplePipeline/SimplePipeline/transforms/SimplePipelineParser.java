package com.wingify.simplePipeline.SimplePipeline.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingify.simplePipeline.SimplePipeline.model.SimplePipelineMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class SimplePipelineParser extends DoFn<PubsubMessage, SimplePipelineMessage> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private Counter counter = Metrics.counter(SimplePipelineParser.class, "pubsub-messages");

    @ProcessElement
    public void processElement(DoFn<PubsubMessage, SimplePipelineMessage>.ProcessContext c) {
        counter.inc();
        PubsubMessage record = c.element();
        byte[] payload = record.getPayload();
        ArrayList<SimplePipelineMessage> msgList = buildSimpleMessage(payload);
        if (msgList != null) {
            msgList.forEach((msg)->c.output(msg) );
        }
    }


    public static ArrayList<SimplePipelineMessage> buildSimpleMessage(byte[] payload) {
        try {
            log.info("payload received : {}", new String(payload));
            JsonNode node = mapper.readTree(payload);
            /* eg key contains a comma seperated list of goal id's for the experiment */
            ArrayList<SimplePipelineMessage> simpleMsgList = new ArrayList<>();
            Integer val = node.get("val").asInt();
            String uuid = node.get("uuid").asText();
            SimplePipelineMessage simplePipelineMessage = SimplePipelineMessage.builder()
                    .val(val)
                    .uuid(uuid)
                    .build();
            simpleMsgList.add(simplePipelineMessage);
            return simpleMsgList;
        } catch (Exception e) {
            log.error("Got error when trying to build campaign message", e);
        }
        return null;
    }



}
