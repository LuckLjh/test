package com.cesgroup.api.store;

import javax.activation.DataSource;

/**
 * 保存对象DTO
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class StoreDTO {

    private String model;

    private String key;

    private DataSource dataSource;

    private String displayName;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
