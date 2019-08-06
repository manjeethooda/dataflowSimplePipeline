package com.wingify.simplePipeline.SimplePipeline;

import com.wingify.simplePipeline.SimplePipeline.datastore.configuration.ConfigProps;
import com.wingify.simplePipeline.SimplePipeline.datastore.configuration.CustomDataFlowOptions;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.runners.dataflow.options.DataflowPipelineWorkerPoolOptions;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

public interface IPipeline extends Serializable {
	CustomDataFlowOptions options = PipelineOptionsFactory.create().as(CustomDataFlowOptions.class);
	PropertiesConfiguration props = new PropertiesConfiguration();
	ConfigProps configProps = new ConfigProps();
	/**
	 * Default method which all pipelines implement that
	 * loads up the configuration.
	 * This can be overridden if a pipeline's config loading mechanism
	 * needs to be tweaked, but ideally that should not happen,
	 * and pipeline behaviour itself should be configurable by the config itself.
	 */
	default void loadDefaultConfig() throws ConfigurationException {
		final String DEFAULT_CONFIG_FILE = "application.properties";
		final String RESOURCE_PATH = "src/main/resources/";
		props.load(RESOURCE_PATH + DEFAULT_CONFIG_FILE);
		Iterator keys = props.getKeys();
		while (keys.hasNext()){
			String key = (String) keys.next();
			String value = (String) props.getProperty(key);
			configProps.addToMap(key,value);
		}
	}

	default List<String> getPropertiesDetail(){
		List<String> details = new ArrayList<>();
		String empty = "";
      	for (int i = 1 ; i <= 110 ; i++ )empty += "_";
      	for (Map.Entry<String, String> entry : configProps.getMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.equals("")
					&& !Pattern.compile(Pattern.quote("pass"), Pattern.CASE_INSENSITIVE).matcher(key).find()) {
            	String pair = key + " : " + value;
				details.add(empty.substring(0,110-pair.length()) + " " + pair);
            }
        }
        return details;
    }

	default DataflowPipelineOptions loadDefaultPipelineOptions(Boolean isPubsubEmulator) {
		if (isPubsubEmulator) {
			String pubsubEmulatorHost = (String) props.getProperty("pubsub.emulator.host");
			String pubsubEmulatorPort = (String) props.getProperty("pubsub.emulator.port");
			options.setPubsubRootUrl("http://"+pubsubEmulatorHost+":"+pubsubEmulatorPort+"/");
		}
		options.setAppName(this.getClass().getSimpleName());
		options.setRunner(DirectRunner.class);
		options.setProps(configProps);
		options.setPropsDetail(getPropertiesDetail());
		return options;
	}

	default DataflowPipelineOptions loadProductionPipelineOptions() {
		String dfRegion = (String) props.getProperty("df.region");
		String dfProjectVersion = (String) props.getProperty("df.projectVersion");
		String project = (String) props.getProperty("gcp.projectID");
		int dfNumWorkers = Integer.parseInt((String) props.getProperty("df.numWorkers"));
		int dfMaxNumWorkers = Integer.parseInt((String) props.getProperty("df.maxNumWorkers"));
		String dfWorkerMachineType = (String) props.getProperty("df.workerMachineType");
		options.setRunner(DataflowRunner.class);
		options.setStreaming(true);
		options.setAutoscalingAlgorithm(DataflowPipelineWorkerPoolOptions.AutoscalingAlgorithmType.THROUGHPUT_BASED);
		options.setMaxNumWorkers(dfMaxNumWorkers);
		options.setWorkerMachineType(dfWorkerMachineType);
		options.setProject(project);
		options.setJobName(this.getClass().getSimpleName() + dfProjectVersion);
		options.setRegion(dfRegion);
		options.setNumWorkers(dfNumWorkers);
		options.setExperiments(new ArrayList<String>(Collections.singleton("enable_stackdriver_agent_metrics")));
		options.setProps(configProps);
		options.setPropsDetail(getPropertiesDetail());
		return options;
	}

	default boolean init() {
		try {
			loadDefaultConfig();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return false;
		}
		boolean isProd = Boolean.parseBoolean((String)props.getProperty("isProd"));
		boolean isPubsubEmulator = Boolean.parseBoolean((String)props.getProperty("isPubsubEmulator"));
		if (isProd && !isPubsubEmulator) {
			loadProductionPipelineOptions();
		} else {
			loadDefaultPipelineOptions(isPubsubEmulator);
		}
		return true;
	}
	
	void run();

}
