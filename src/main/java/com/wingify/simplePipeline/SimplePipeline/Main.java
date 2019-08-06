package com.wingify.simplePipeline.SimplePipeline;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        IPipeline pipeline = new SimplePipeline();
        if(!pipeline.init()) {
            log.error("failed to init, exiting!");
            return;
        }
        pipeline.run();
    }
}
