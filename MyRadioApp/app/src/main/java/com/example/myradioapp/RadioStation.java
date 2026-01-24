package com.example.myradioapp;

public class RadioStation {
    private String name;
    private String streamUrl;
    public RadioStation(String name, String streamUrl) {
        this.name = name;
        this.streamUrl = streamUrl;
    }

    public String getName() { return name; }
    public String getStreamUrl() { return streamUrl; }
}
