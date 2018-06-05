package com.example.recordvoice.Model;

/**
 * Created by vieta on 18/11/2016.
 */
public class CloudRecord {
    private String name;
    private long size;

    public CloudRecord(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
