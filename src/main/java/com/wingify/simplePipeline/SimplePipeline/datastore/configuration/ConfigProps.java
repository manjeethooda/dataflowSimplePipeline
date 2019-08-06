package com.wingify.simplePipeline.SimplePipeline.datastore.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ConfigProps implements Serializable {

    private Map<String, String> map;

    public ConfigProps() {
        map = new HashMap<String, String>();
    }

    public void addToMap(String key, String value) {
        this.map.put(key, value);
    }

    public Map<String, String> getMap() {
        return this.map;
    }

}
