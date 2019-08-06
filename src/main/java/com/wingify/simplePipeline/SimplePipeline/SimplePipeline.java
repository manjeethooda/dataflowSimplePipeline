package com.wingify.simplePipeline.SimplePipeline;

import com.wingify.simplePipeline.SimplePipeline.model.SimplePipelineMessage;
import com.wingify.simplePipeline.SimplePipeline.transforms.SimplePipelineParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.joda.time.Duration;

@Slf4j
public class SimplePipeline implements IPipeline {

    public SimplePipeline() {
        log.info("initializing simplePipeline");

    }
    @Override
    public void run() {
        log.info("running simplePipeline");

        final String SIMPLE_PIPELINE_SUBSCRIPTION = "";
        Pipeline pipeline = Pipeline.create(this.options);

                // Calculate 1-minute counts of events per user.
                // stage 1 : reading from pubsub
                pipeline.apply(PubsubIO.readMessagesWithAttributes()
                        .fromSubscription(SIMPLE_PIPELINE_SUBSCRIPTION)
                        .withIdAttribute("m_id"))

                        // stage 2 : passing through parser
                        .apply(ParDo.of(new SimplePipelineParser()))
                        .setCoder(SerializableCoder.of(SimplePipelineMessage.class))

                        // stage 3 : make KV pair
                        .apply(WithKeys.of((SimplePipelineMessage elem) -> elem.getPrimaryRecordKeyString())
                                .withKeyType(TypeDescriptor.of(String.class)))


                        .apply(Window.<KV<String,SimplePipelineMessage>>into(
                                FixedWindows.of(Duration.standardSeconds(1))).triggering(
                                Repeatedly.forever(AfterFirst.of(
                                        AfterPane.elementCountAtLeast(1),
                                        AfterProcessingTime.
                                                pastFirstElementInPane().
                                                plusDelayOf(Duration.standardSeconds(1)))
                                )
                        ).withAllowedLateness(
                                Duration.ZERO
                        ).discardingFiredPanes())
                        
                        .apply("GroupByUuid", GroupByKey.create())
                        .apply(ParDo.of(new DoFn<KV<String, Iterable<SimplePipelineMessage>>, KV<String,String>>() {

                            @ProcessElement
                            public void processElement(ProcessContext c) {
                              Iterable<SimplePipelineMessage> iterable = c.element().getValue();
                              for(SimplePipelineMessage simplePipelineMessage : iterable){
                                  c.output(KV.of(simplePipelineMessage.getUuid(),"1"));
                              }
                            }
                        }))
                        .apply(Reshuffle.viaRandomKey())

                        // @ TODO : add redis host and port before running pipeline
                        .apply("push-to-redis-sink",RedisIO.write().withEndpoint("",).withMethod(RedisIO.Write.Method.INCRBY));

        pipeline.run();
    }
}
