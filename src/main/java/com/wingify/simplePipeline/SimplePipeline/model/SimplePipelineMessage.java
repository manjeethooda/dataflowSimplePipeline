package com.wingify.simplePipeline.SimplePipeline.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Setter
@Getter
@Builder
@Slf4j
public class SimplePipelineMessage implements Serializable{

    private String uuid;

    private Integer val;

    public String getPrimaryRecordKeyString() {
        return String.format("%s_%s", this.uuid, this.val);
    }

}
