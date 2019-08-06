package com.wingify.simplePipeline.SimplePipeline.datastore.configuration;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Description;

import java.util.List;

public interface CustomDataFlowOptions extends DataflowPipelineOptions {

    @Description("Setting Properties")
    ConfigProps getProps();
    void setProps(ConfigProps props);

    @Description("Properties Details")
    List<String> getPropsDetail();
    void setPropsDetail(List<String> propsDetail);

}
